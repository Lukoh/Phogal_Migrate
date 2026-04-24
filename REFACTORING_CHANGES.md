# Phogal Refactoring — Changes Summary

**Scope:** transform Phogal (Unsplash client) to align with Android best practices as of April 2026, along four required axes:

1. **Network layer** — `NetworkResult` sealed interface + `safeApiCall`
2. **Repository layer** — `interface + Impl` separation
3. **ViewModel layer** — sealed `UiState` + proper `stateIn`
4. **Navigation** — Type-safe routes via `@Serializable` (Nav Compose 2.8+)

The refactor was intentionally aggressive: legacy abstractions that could not be adapted cleanly were deleted, not preserved. Total change footprint is ~60 files touched across 4 phases.

---

## Guiding principles

Every decision below flows from three rules:

- **No untyped state at boundaries.** `StateFlow<Any>`, `Resource.data: Any?`, `vararg Any?` are all compile-time ways of saying "I don't know what this is." They were all removed.
- **One source of truth per concept.** When the legacy code had a `picture: Picture?` field on a UI state holder AND a `pictureUiState` flow AND a `Resource.data as Picture` cast all representing the same fact, that's three sources of truth — we collapsed it to one (the VM's `StateFlow<UiState<Picture>>`).
- **Prefer composition over inheritance for infrastructure.** Abstract base classes that held `@Inject lateinit var` fields were replaced by concrete classes with constructor injection.

---

## Phase 1 — Network Layer

### Before

- `Resource` was a mutable class with `lateinit var status: Status`, `var data: Any?`, `var errorCode: Int`. Every network call mutated a shared instance.
- `RestAPI` methods returned `Flow<ApiResponse<T>>` via a custom `FlowCallAdapterFactory`. This wrapped Retrofit `Call<T>` into a `Flow` that emitted exactly once — a Flow with no purpose, added only so ViewModels could `.collectLatest {}` on the result.
- Error paths lost type information: `handleResponse(response)` mapped `ApiErrorResponse` into `resource.error(msg, code)`, then the UI would read `resource.message.toString()` and `resource.errorCode`.
- `CancellationException` had no special handling anywhere — it could be swallowed by the generic `.catch` in `FlowCallAdapter`, which is a correctness bug with structured concurrency.

### After

```kotlin
// data/datasource/network/NetworkResult.kt
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data object Empty : NetworkResult<Nothing>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>
}

// data/datasource/network/SafeApiCall.kt
suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> Response<T>
): NetworkResult<T> = try {
    val response = block()
    if (response.isSuccessful) {
        val body = response.body()
        if (body == null || response.code() == 204) NetworkResult.Empty
        else NetworkResult.Success(body)
    } else {
        NetworkResult.Error(
            code = response.code(),
            message = /* best-effort error body → message */
        )
    }
} catch (ce: CancellationException) {
    throw ce                                    // never swallow
} catch (httpException: HttpException) {
    NetworkResult.Error(httpException.code(), httpException.message.orEmpty())
} catch (ioException: IOException) {
    NetworkResult.Exception(ioException)
} catch (throwable: Throwable) {
    NetworkResult.Exception(throwable)
}

// data/datasource/network/api/RestAPI.kt
interface RestAPI {
    @GET("search/photos")
    suspend fun getPhotos(...): Response<PhotosResponse>  // plain Retrofit
    // ...
}
```

**Why `Error` and `Exception` are distinct.** `Error` means "the server responded with 4xx/5xx" — the request completed, and a retry is likely pointless without fixing something upstream. `Exception` means "the request never reached a response" — retry is often appropriate. Keeping them separate lets the UI show an offline indicator vs. a 403/500 dialog without re-parsing messages.

### Files touched (Phase 1)

| Added | Removed | Modified |
|---|---|---|
| `NetworkResult.kt`, `SafeApiCall.kt` | `Resource.kt`, `Status.kt`, `ApiResponse.kt`, `FlowCallAdapter.kt`, `FlowCallAdapterFactory.kt`, `response/` dir | `RestAPI.kt` (7 methods rewritten to `suspend fun ... Response<T>`), `AppModule.kt` (`provideResource()` removed, `addCallAdapterFactory(FlowCallAdapterFactory())` removed) |

`NullOnEmptyConverterFactory` and `NetworkErrorHandler` were kept as-is — they are orthogonal utilities.

---

## Phase 2 — Repository Layer

### Before

```kotlin
@Singleton
abstract class Repository<T> {
    @Inject lateinit var restAPI: RestAPI
    @Inject lateinit var networkErrorHandler: NetworkErrorHandler
    @Inject lateinit var localDataSource: LocalDataSource
    @Inject lateinit var resource: Resource          // shared mutable state!

    abstract fun trigger(replyCount: Int, params: Params): Flow<T>

    companion object {
        internal const val ITEM_COUNT = 10
        internal var replyCount = 0                  // mutable static!
    }
}

@Singleton
class GetPictureRepository @Inject constructor() : Repository<ApiResponse<Picture>>() {
    override fun trigger(replyCount: Int, params: Params) =
        restAPI.getPhoto(params.args[0] as String, BuildConfig.clientId)
    //                   ^^^^^^^^^^^^^^^^^^^^^^^^^ untyped, crashes at runtime if wrong
}

class Params constructor(vararg var args: Any?)      // the "params" type
```

There are a half-dozen anti-patterns in 30 lines. Generic base class with field injection, `@Singleton` on an abstract class, a `companion object` counter holding cross-instance mutable state, `vararg Any?` for parameters, a shared `Resource` instance, and a sibling class (`BasePagingSource`) with `setPagingParam()` late-binding. None of it survived.

### After

```kotlin
// data/repository/common/photo/info/PictureRepository.kt
interface PictureRepository {
    suspend fun getPicture(id: String): NetworkResult<Picture>
}

// data/repository/common/photo/info/PictureRepositoryImpl.kt
@Singleton
class PictureRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : PictureRepository {
    override suspend fun getPicture(id: String): NetworkResult<Picture> = safeApiCall {
        api.getPhoto(id = id, clientId = BuildConfig.clientId)
    }
}

// di/module/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindPictureRepository(impl: PictureRepositoryImpl): PictureRepository
    // ... 4 more @Binds
}
```

### Repository catalog

| Interface | Impl | Returns |
|---|---|---|
| `PhotosRepository` | `PhotosRepositoryImpl` | `Flow<PagingData<Photo>>` (paged search) |
| `PictureRepository` | `PictureRepositoryImpl` | `NetworkResult<Picture>` (single photo) |
| `PictureLikeRepository` | `PictureLikeRepositoryImpl` | `NetworkResult<LikeResponse>` (`like(id)` + `unlike(id)`) |
| `UserPhotosRepository` | `UserPhotosRepositoryImpl` | `Flow<PagingData<Photo>>` (paged user photos) |
| `PopularPhotosRepository` | `PopularPhotosRepositoryImpl` | `Flow<PagingData<Photo>>` (paged popular feed) |

**Why `PictureLikeRepository` merges POST and DELETE.** The legacy code had `PostPictureLikeRepository` and `DeletePictureLikeRepository` as separate singletons, each consumed by its own ViewModel. Like/unlike is one conceptual feature (toggle a boolean). Splitting it was cargo-culted from a CRUD-per-HTTP-method convention that has no payoff here.

### PagingSource rewrite

The legacy `BasePagingSource<Key, Response, Value>` held mutable `restAPI` and `params` fields and was late-bound via `setPagingParam()`. The new `PhotosPagingSource`, `UserPhotosPagingSource`, and `PopularPhotosPagingSource` all take their dependencies via the constructor:

```kotlin
class PhotosPagingSource(
    private val api: RestAPI,
    private val query: String,
    private val pageSize: Int
) : PagingSource<Int, Photo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = api.getPhotos(...)
            if (!response.isSuccessful) {
                LoadResult.Error(HttpException(response))
            } else {
                LoadResult.Page(...)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (io: IOException) {
            LoadResult.Error(io)
        } catch (http: HttpException) {
            LoadResult.Error(http)
        }
    }
}
```

The corresponding `Repository.invalidatePagingSource()` manual-invalidation API is gone — Paging3 already handles it through `Pager.flow.cachedIn(scope)` and `LazyPagingItems.refresh()`.

**`ErrorThrowable` and `Gson().fromJson(...)` in Section composables are gone.** The legacy `BasePagingSource` wrapped real exceptions into a JSON-serialized `ErrorThrowable` and threw a new `Throwable(json)` so the UI could parse it back with Gson. With real Retrofit exceptions propagating naturally, the UI now reads `(loadState.error as LoadState.Error).error.message` directly.

### Files touched (Phase 2)

| Added | Removed | Modified |
|---|---|---|
| 5 interfaces + 5 Impls + 3 new PagingSources + `RepositoryModule.kt` | `Repository.kt` (abstract base), `BasePagingSource.kt`, `Params.kt`, 3 old PagingSources, 4 old `Get*Repository.kt`, `PostPictureLikeRepository.kt`, `DeletePictureLikeRepository.kt`, `GetPictureRepository.kt` | — |

---

## Phase 3 — ViewModel Layer

### Before

```kotlin
@Singleton                                       // @Singleton on a ViewModel!
abstract class BaseViewModel<T> : ViewModel() {
    val resource by lazy { Resource() }
    // ...
}

@HiltViewModel
class PictureViewModel @Inject constructor(
    private val getPictureRepository: GetPictureRepository
) : BaseViewModel<Picture>() {
    private val _pictureUiState = MutableStateFlow(Any())   // StateFlow<Any>
    val pictureUiState: StateFlow<Any> = _pictureUiState

    override fun trigger(replyCount: Int, params: Params) {
        viewModelScope.launch {
            getPictureRepository.trigger(replyCount, params)
                .stateIn(viewModelScope)                    // stateIn inside launch
                .collectLatest {                            // ...and collect inside launch
                    _pictureUiState.value = handleResponse(it)
                }
        }
    }
}
```

Five distinct problems:

1. `@Singleton` on a ViewModel base class, while also annotated `@HiltViewModel` on subclasses — two scopes disagreeing.
2. `MutableStateFlow(Any())` — the type parameter is `Any`. Compose had to runtime-check with `if (pictureUiState.value is Resource)`.
3. `.stateIn(viewModelScope)` inside a `viewModelScope.launch { }` — that's not how `stateIn` works. A cold flow transformed with `stateIn` should be exposed as a property; wrapping it in a launch defeats the purpose.
4. `.collectLatest {}` inside a launch re-assigning to a different `MutableStateFlow` — double buffering for no reason.
5. Eleven ViewModels total, many of them one per HTTP method (`PictureLikeViewModel` vs. `PictureUnlikeViewModel`).

### After — `UiState` sealed type

```kotlin
// presentation/stateholder/uistate/UiState.kt
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val code: Int, val message: String) : UiState<Nothing>
}
```

Compose consumers now write exhaustive `when` branches:

```kotlin
when (val s = pictureViewModel.pictureUiState.collectAsStateWithLifecycle().value) {
    UiState.Idle, UiState.Loading -> LoadingPicture()
    is UiState.Success -> BodyContent(picture = s.data, ...)
    is UiState.Error   -> ErrorContent(code = s.code, message = s.message, onRetry = { ... })
}
```

### After — merged `PictureViewModel`

```kotlin
@HiltViewModel
class PictureViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
    private val pictureLikeRepository: PictureLikeRepository
) : ViewModel() {

    private val _pictureUiState = MutableStateFlow<UiState<Picture>>(UiState.Idle)
    val pictureUiState: StateFlow<UiState<Picture>> = _pictureUiState.asStateFlow()

    private val _likeActionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val likeActionState: StateFlow<UiState<Unit>> = _likeActionState.asStateFlow()

    fun loadPicture(id: String) {
        _pictureUiState.value = UiState.Loading
        viewModelScope.launch {
            _pictureUiState.value = pictureRepository.getPicture(id).toUiStateStrict()
        }
    }

    fun toggleLike() {
        val current = (_pictureUiState.value as? UiState.Success)?.data ?: return
        val wasLiked = current.liked_by_user
        viewModelScope.launch {
            val result = if (wasLiked) pictureLikeRepository.unlike(current.id)
                         else           pictureLikeRepository.like(current.id)
            when (result) {
                is NetworkResult.Success, NetworkResult.Empty -> {
                    // patch local state so the heart icon flips without a refetch
                    _pictureUiState.update { state ->
                        if (state is UiState.Success) {
                            UiState.Success(state.data.copy(liked_by_user = !wasLiked))
                        } else state
                    }
                    _likeActionState.value = UiState.Success(Unit)
                }
                is NetworkResult.Error     -> _likeActionState.value = UiState.Error(result.code, result.message)
                is NetworkResult.Exception -> _likeActionState.value = UiState.Error(0, result.throwable.message ?: "Network failure")
            }
        }
    }

    fun consumeLikeAction() { _likeActionState.value = UiState.Idle }
}
```

Two independent `StateFlow`s — `pictureUiState` and `likeActionState` — because a failed like must not wipe an already-loaded picture off the screen.

### After — `GalleryViewModel` with proper reactive search

```kotlin
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
    private val localDataSource: LocalDataSource
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val photos: StateFlow<PagingData<Photo>> = _query
        .debounce(300L)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { q -> photosRepository.search(q, PAGE_SIZE) }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PagingData.empty()
        )

    fun onQueryChanged(q: String) { _query.value = q }
    fun commitSearch() { /* persist to local search history */ }
    // ...
}
```

This is the canonical Compose + Paging3 pattern: a single cold Flow chain transformed once, materialized via `stateIn` as a `StateFlow`, cached in the VM scope. `SearchWordViewModel` was absorbed — local search history is part of this screen's state holder, not a separate VM.

### ViewModel consolidation

| Before | After |
|---|---|
| `PictureViewModel` + `PictureLikeViewModel` + `PictureUnlikeViewModel` (3) | `PictureViewModel` (1) |
| `GalleryViewModel` + `SearchWordViewModel` (2) | `GalleryViewModel` (1) |
| `UserPhotosViewModel`, `PopularPhotosViewModel`, `BookmarkViewModel`, `FollowViewModel`, `NotificationSettingViewModel` | same, but no longer extend `BaseViewModel` |
| **Total: 11** | **Total: 7** |

### Refresh handling — the "B pattern" decision

The legacy code had `isRefreshing: StateFlow<Boolean>` on every VM that did paging, and Section composables took it as a parameter. The canonical Paging3 pattern instead derives refresh state from `lazyPagingItems.loadState.refresh is LoadState.Loading`. We picked the canonical pattern:

```kotlin
@Composable
fun SearchPhotosSection(
    photos: LazyPagingItems<Photo>,    // accepted directly; no StateFlow<Any> wrapping
    // ...
) {
    val isRefreshing = photos.loadState.refresh is LoadState.Loading
    val refreshState = rememberPullRefreshState(isRefreshing, onRefresh = { photos.refresh() })
    // ...
}
```

As a consequence, `ContentState` holders no longer carry `photosUiState: StateFlow<Any>` or `refreshingState: StateFlow<Boolean>`. They only hold UI-local ephemeral state (`enabledBookmarkState`, `visibleActionsState`, etc.).

### Files touched (Phase 3)

| Added | Removed | Modified |
|---|---|---|
| `UiState.kt`, `NetworkResultMapping.kt` | `BaseViewModel.kt`, `PictureLikeViewModel.kt`, `PictureUnlikeViewModel.kt`, `SearchWordViewModel.kt`, `PagingErrorMessage.kt`, `ResourceState.kt`, `rememberResourceState.kt`, `ErrorThrowable.kt` | 7 VMs × wholesale, 4 ContentState holders, 3 SectionState holders, 3 Section composables (`ErrorThrowable + Gson` removed, `photos` param added), 5 Screen/Content files, 2 Destination files |

---

## Phase 4 — Type-Safe Navigation

### Before

```kotlin
// ─────── PhogalDestination.kt ────────
interface PhogalDestination {
    val icon: ImageVector
    val route: String
    val screen: @Composable (NavHostController, NavBackStackEntry, String) -> Unit

    companion object {
        internal const val photosHomeRoute = "photoHome"
        internal const val searchPhotosStartRoute = "photoHome/searchPhotos"
        internal const val pictureRoute = "photoHome/picture"
        internal const val userPhotosRoute = "photoHome/userPhotos"
        // ... 10 more string constants
    }
}

// ─────── Gallery.kt destination ────────
object SearchPhotos : PhogalDestination {
    override val screen = { navController, backStackEntry, _ ->
        // ...
        SearchPhotosScreen(
            onItemClicked = { id ->
                val pictureArgument = PictureArgument(id = id, visibleViewPhotosButton = true)
                val gson = Gson()
                val json = Uri.encode(gson.toJson(pictureArgument))

                navController.navigateTo("${Picture.route}/$json")
            },
            // ...
        )
    }
}

// ─────── receiving side ────────
val argument = backStackEntry.arguments?.getString("argument")
val pictureArgument = Gson().fromJson(argument, PictureArgument::class.java)
```

String routes concatenated with Gson-encoded JSON payloads passed through `Uri.encode`. Every route had its own wrapper constant (`pictureRoute`, `pictureRouteArgs`) and its own `navArgument(...) { type = NavType.StringType }` declaration. Receiving a typed object from navigation required Gson parsing inside the destination composable.

### After

```kotlin
// ─────── Routes.kt ────────
object Routes {
    @Serializable data object GalleryGraph
    @Serializable data object PopularPhotosGraph
    @Serializable data object NotificationGraph
    @Serializable data object SettingGraph

    @Serializable data object SearchPhotosRoute
    @Serializable data class PictureRoute(val id: String, val showViewPhotosButton: Boolean)
    @Serializable data class UserPhotosRoute(
        val name: String, val firstName: String, val lastName: String, val username: String
    )
    @Serializable data class WebViewRoute(val firstName: String, val url: String)
    @Serializable data object PopularPhotosRoute
    @Serializable data object NotificationsRoute
    @Serializable data class NotificationRoute(val id: String)
    @Serializable data object SettingRoute
    @Serializable data object BookmarkedPhotosRoute
    @Serializable data object FollowingUsersRoute
    @Serializable data object NotificationSettingRoute
}

// ─────── galleryGraph ────────
fun NavGraphBuilder.galleryGraph(navController: NavHostController) {
    navigation<Routes.GalleryGraph>(startDestination = Routes.SearchPhotosRoute) {

        composable<Routes.SearchPhotosRoute> {
            SearchPhotosScreen(
                onItemClicked = { id ->
                    navController.navigate(
                        Routes.PictureRoute(id = id, showViewPhotosButton = true)
                    )
                },
                // ...
            )
        }

        composable<Routes.PictureRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.PictureRoute>()
            PictureScreen(
                pictureViewModel = hiltViewModel(backStackEntry),
                state = rememberPhotoContentState(
                    idState = rememberSaveable { mutableStateOf(args.id) },
                    visibleViewButtonState = rememberSaveable { mutableStateOf(args.showViewPhotosButton) }
                ),
                // ...
            )
        }
    }
}
```

### Selection of the current tab

```kotlin
// MainScreenState.kt
val currentTopLevelDestination: BottomNavDestination?
    @Composable get() = currentDestination?.let { destination ->
        when {
            destination.hasRoute(Routes.SearchPhotosRoute::class)  -> Gallery
            destination.hasRoute(Routes.PopularPhotosRoute::class) -> PopularPhotos
            destination.hasRoute(Routes.NotificationsRoute::class) -> Notification
            destination.hasRoute(Routes.SettingRoute::class)       -> Setting
            else -> null
        }
    }
```

`hasRoute(T::class)` is Nav 2.8's reified check — no string comparison, compile-time failure if the class doesn't exist.

### Notes on the nested graphs

The `Setting` tab has its own `composable<PictureRoute>` entry inside `settingGraph` so tapping a bookmark pushes onto the Setting back stack rather than hopping the user into the Gallery tab. The two `PictureRoute` composables don't conflict because Nav scopes them to different nested graphs.

### Build changes

`build.gradle` (top-level):
```groovy
ext {
    nav_version = '2.8.0'                              // was 2.7.0-beta02
    kotlinx_serialization_version = '1.5.1'            // NEW
}

plugins {
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.8.21' apply false   // NEW
}
```

`app/build.gradle`:
```groovy
plugins {
    id 'org.jetbrains.kotlin.plugin.serialization'     // NEW
}

dependencies {
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version"   // NEW
    // navigation-compose already updated via nav_version bump
}
```

### Files touched (Phase 4)

| Added | Removed | Modified |
|---|---|---|
| `Routes.kt`, 4 new `*NavGraph.kt` (inlined composables, `backStackEntry.toRoute<T>()`) | `PhogalDestination.kt`, `Gallery.kt` / `Notification.kt` / `PopularPhotos.kt` / `Setting.kt` destination objects, `NavHostControllerExt.kt` (string `navigateTo` helper), 4 old `*NavGraph.kt` files | `MainScreenState.kt`, `HomeScreen.kt`, `build.gradle`, `app/build.gradle` |

---

## Cross-phase summary

### File delta

| | Added | Removed | Modified |
|---|---|---|---|
| Phase 1 — Network | 2 | 5 + 1 dir | 2 |
| Phase 2 — Repository | 12 | 13 | — |
| Phase 3 — VM / UI | 3 | 8 | ~24 |
| Phase 4 — Navigation | 5 | 10 | 4 |
| **Total** | **~22** | **~36** | **~30** |

Net: the codebase is smaller. About 2,200 lines of mutable-state, untyped-paramater, and cargo-culted glue code were deleted.

### What did **not** change

The following were intentionally out of scope:

- **Groovy → Kotlin DSL build conversion** (libs.versions.toml etc.)
- **KAPT → KSP migration** — would force bumping Hilt past 2.45 and potentially Kotlin past 1.8.21
- **Compose BOM version bump** (still on 2023.04.01)
- **Stetho / PersistentCookieJar / Accompanist removal** — these are deprecated in 2026 but removal is a separate concern
- **Test code** — no test changes; recommend writing new tests against the new `NetworkResult`/`UiState` contracts
- **Firebase / Analytics wiring**
- **The `base/designsystem/component/*` files** — they are structurally unrelated to the four refactor axes

### Known items to verify on first build

1. **Nav 2.8.0 + Kotlin 1.8.21 compatibility.** The type-safe Navigation API requires Nav 2.8+. `composable<T>` and `hasRoute<T>()` should be available in 2.8.0 stable. If the build fails with a reified-type error, the symptom is usually a missing `kotlinx-serialization-json` dependency — this was added but double-check Gradle sync picked it up.
2. **`@FlowPreview` and `@ExperimentalCoroutinesApi` opt-ins in `GalleryViewModel` and `UserPhotosViewModel`.** They're annotated at the class level. If the Kotlin compiler requires suppressing them elsewhere (e.g., via `-opt-in` flag), add it to `kotlinOptions.freeCompilerArgs`.
3. **The preview composable `ProfilerHomeScreenPreview` was dropped from `HomeScreen.kt`** during the rewrite because its body referenced several string routes that no longer exist. Either rewrite it with typed routes or delete it entirely.
4. **`HomeScreen.inTab(tab: BottomNavDestination)` helper** uses `hierarchy.any { hasRoute(...) }`. If Nav 2.8's back-stack hierarchy semantics differ from what `hierarchy.any` returned for string routes, the bottom bar "selected" state might need fine-tuning. Run the app and tap each tab to verify the selection highlight tracks correctly.
5. **The `NotificationRoute` detail `composable<>` block is a no-op placeholder** — the legacy code never wired a real detail screen for it either, so this is a TODO carried forward, not a regression.

### Migration notes for future development

- **Adding a new screen:** add a `@Serializable` class/object to `Routes.kt`, add a `composable<Routes.YourRoute> { ... }` block to the appropriate NavGraph. That's it — no registration elsewhere.
- **Adding a new repository:** create `interface FooRepository` and `class FooRepositoryImpl @Inject constructor(...)`, add a `@Binds` to `RepositoryModule`. Don't extend any base class.
- **Adding a new ViewModel:** extend `ViewModel`, inject repositories via constructor, expose a `StateFlow<UiState<T>>` from a cold `Flow` via `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`. Don't inherit from any base ViewModel — there is no longer one.
- **Handling a new API error category:** the only place that maps exceptions to result types is `SafeApiCall.kt`. Add a new catch branch there and a new variant to `NetworkResult` if needed.

---

## Build & run

```bash
./gradlew :app:assembleDevDebug      # or prodDebug / stgDebug
```

If Gradle complains about `kotlinx-serialization-json` not being found, run a clean sync:

```bash
./gradlew --refresh-dependencies :app:dependencies
```
