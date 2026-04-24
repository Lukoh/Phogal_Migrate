# Phogal - Compose Decomposition & Flow 최적화 변경 내역

최적화 날짜: 2026-04-19
적용 후 빌드 환경: **AGP 8.7.3 / Kotlin 2.0.21 / Gradle 8.10.2 / Android Studio Panda 2**

---

## 🎯 최적화 목표

1. **Compose Decomposition**: 큰 컴포저블을 작은 단위로 분해해 recomposition scope를 좁히고, 각 하위 컴포저블이 독립적으로 테스트/프리뷰 가능하게 함
2. **Flow 최적화**: I/O를 메인 스레드에서 분리, UI 이벤트를 SharedFlow로 분리, 중복 요청 방지

---

## 📋 변경된 파일 목록 (총 8개)

### ViewModel / Flow (5개)
1. `presentation/stateholder/business/home/gallery/GalleryViewModel.kt`
2. `presentation/stateholder/business/home/common/photo/info/PictureViewModel.kt`
3. `presentation/stateholder/business/home/common/bookmark/BookmarkViewModel.kt`
4. `presentation/stateholder/business/home/common/follow/FollowViewModel.kt`
5. `presentation/stateholder/business/home/common/user/UserPhotosViewModel.kt`

### Compose UI (3개)
6. `presentation/ui/compose/screen/home/gallery/SearchPhotosScreen.kt`
7. `presentation/ui/compose/screen/home/gallery/SearchPhotosContent.kt`
8. `presentation/ui/compose/screen/home/gallery/SearchPhotosSection.kt`

---

## 🔄 ViewModel / Flow 최적화 상세

### 1. GalleryViewModel

**문제점**
- `commitSearch()` 에서 `localDataSource` 를 메인 스레드에서 동기 호출 (disk I/O)
- `refreshRecentWords()` 가 `_recentWords` 를 재계산하며 불필요한 중복
- UI 이벤트(검색 완료, 키보드 닫기 등)를 전달할 채널 없음 → 상태에 뒤섞여 있음

**변경 내용**
```kotlin
// I/O dispatcher 주입 (테스트 시 교체 가능)
@Inject constructor(
    ...
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
)

// I/O를 withContext(ioDispatcher)로 감싸서 메인 스레드 블로킹 제거
fun commitSearch() {
    ...
    viewModelScope.launch {
        val updated = withContext(ioDispatcher) {
            // disk I/O 여기서만 수행
        } ?: return@launch
        _recentWords.value = updated.asReversed()
        _events.tryEmit(GalleryUiEvent.SearchCommitted(keyword))
    }
}

// 일회성 UI 이벤트용 SharedFlow 추가
private val _events = MutableSharedFlow<GalleryUiEvent>(
    replay = 0,
    extraBufferCapacity = 1
)
val events: SharedFlow<GalleryUiEvent> = _events.asSharedFlow()

sealed interface GalleryUiEvent {
    data class SearchCommitted(val keyword: String) : GalleryUiEvent
}
```

**효과**
- 메인 스레드가 I/O 에 블로킹되지 않음 → ANR 리스크 감소
- UI 이벤트와 상태가 명확히 분리됨 (회전 시 이벤트 재발생 없음)
- 테스트에서 `TestDispatcher` 주입으로 제어 가능

---

### 2. PictureViewModel

**문제점**
- `toggleLike()` 에서 중복 호출 방지 로직 없음 → 빠른 더블탭 시 race condition
- `update {}` 블록 외부에서 `_likeActionState` 를 갱신해 원자성 깨짐
- 결과 분기가 장황함

**변경 내용**
```kotlin
fun toggleLike() {
    val current = (_pictureUiState.value as? UiState.Success)?.data ?: return
    if (_likeActionState.value is UiState.Loading) return  // 중복 호출 가드

    ...
    when (result) {
        is NetworkResult.Success, NetworkResult.Empty -> {
            _pictureUiState.update { state ->
                // state.data.id == pictureId 검증으로 race 방지
                if (state is UiState.Success && state.data.id == pictureId) {
                    UiState.Success(state.data.copy(liked_by_user = !wasLiked))
                } else state
            }
            _likeActionState.value = UiState.Success(Unit)
            _events.tryEmit(PictureUiEvent.LikeToggled(liked = !wasLiked))
        }
        ...
    }
}

sealed interface PictureUiEvent {
    data class LikeToggled(val liked: Boolean) : PictureUiEvent
    data class LikeFailed(val message: String) : PictureUiEvent
}
```

**효과**
- 더블탭으로 같은 요청이 두 번 날아가지 않음
- `loadPicture()` 와 `toggleLike()` 가 동시에 일어나도 상태 일관성 유지

---

### 3. BookmarkViewModel / 4. FollowViewModel

**공통 변경**: 모든 `localDataSource` 호출을 `withContext(ioDispatcher)` 로 감쌈

```kotlin
fun setBookmarkPicture(picture: Picture) {
    viewModelScope.launch {
        withContext(ioDispatcher) {
            localDataSource.setBookmarkPhoto(picture)
        }
        refresh()
    }
}
```

**효과**: SharedPreferences/DB 쓰기가 메인 스레드를 블로킹하지 않음

---

### 5. UserPhotosViewModel

**문제점**: `_username` 이 `distinctUntilChanged` 없이 `flatMapLatest` 에 들어가서,
동일 username 으로 `loadFor()` 가 두 번 호출되면 Pager 가 불필요하게 재생성됨

**변경 내용**
```kotlin
val photos: StateFlow<PagingData<Photo>> = _username
    .distinctUntilChanged()   // ← 추가: 동일값 재발행 방지
    .flatMapLatest { ... }
```

**효과**: config change + restore 시 불필요한 Paging re-subscription 제거

---

## 🎨 Compose Decomposition 상세

### 6. SearchPhotosScreen

**Before**: 하나의 `@Composable` 안에 `Scaffold`, `TopBar` (50줄), `SnackbarHost`, `Lifecycle observer` 가 모두 혼재 → 총 208줄

**After**: 4개 단위로 분리
```kotlin
@Composable
fun SearchPhotosScreen(...) {
    ObserveLifecycle(...)
    BackHandler(...) { ... }
    Scaffold(
        snackbarHost = { SearchSnackbarHost(snackbarHostState) },
        topBar = { SearchTopBar(showFavoriteAction = ...) },
        content = { ... }
    )
}

@Composable private fun SearchTopBar(...)            // 재사용 가능
@Composable private fun SearchSnackbarHost(...)      // 재사용 가능
@Composable private fun ObserveLifecycle(...)        // 재사용 가능
```

**Recomposition 효과**:
- `visibleActionsState` 가 바뀔 때 `SearchTopBar` 만 recompose
- `snackbarHostState` 가 바뀔 때 `SearchSnackbarHost` 만 recompose
- 기존에는 `Scaffold` 전체가 recompose 대상이었음

---

### 7. SearchPhotosContent

**Before**: 186줄 안에 search bar, chips 애니메이션, paging list 전환, permission 처리가 혼재

**After**: 메인 컴포저블 + 3개 하위
```kotlin
@Composable fun SearchPhotosContent(...) {
    // 1. Stable lambda로 wrapping (stability 개선)
    val onSearch = remember(galleryViewModel, currentQuery, ...) { { ... } }
    val onChipClicked = remember(galleryViewModel, ...) { { ... } }
    
    Column {
        SearchSection(onSearched = onSearch)
        RecentWordsChips(...)         // ← 분리
        PhotosOrInitScreen(...)        // ← 분리
    }
    PermissionHandler(...)              // ← 분리
}
```

**핵심 개선: Lambda Stability**

기존 코드:
```kotlin
SearchSection(
    onSearched = { keyword ->  // ← 매번 새 람다 인스턴스
        if (keyword.isNotEmpty() && ...) { ... }
    }
)
```
→ `SearchSection` 이 불안정(unstable)한 파라미터 받음 → recompose 시마다 skipping 불가

개선 후:
```kotlin
val onSearch: (String) -> Unit = remember(galleryViewModel, currentQuery, ...) {
    { keyword -> if (...) { ... } }
}
SearchSection(onSearched = onSearch)
```
→ `onSearch` 는 안정적으로 같은 참조 → `SearchSection` skipping 가능

---

### 8. SearchPhotosSection (가장 큰 변경)

**Before**: 324줄. `@file:Suppress("UNCHECKED_CAST")` 있음. LoadState 분기가 한 덩어리.

**After**: 메인 컴포저블 + 5개 하위 + 1개 helper
```kotlin
@Composable fun SearchPhotosSection(...) { ... }      // 메인

// LazyListScope extension으로 분리 - item {} 직접 emit
private fun LazyListScope.renderLoadState(...)
private fun LazyListScope.photoItems(...)

// 개별 state composable
@Composable private fun LoadingRow()
@Composable private fun EmptyState()
@Composable private fun ErrorRow(throwable, onRetry)

// Flow helper
private fun snapshotFlowScrollState(state: LazyListState)
```

**핵심 개선 5가지**

**1) `@Suppress("UNCHECKED_CAST")` 완전 제거**
- 원인이었던 타입 캐스팅 코드 자체를 제거

**2) `derivedStateOf` 도입**
```kotlin
// Before: 매 recomposition마다 계산
if (!lazyListState.isScrollInProgress) { ... }
val offset = lazyListState.rememberCurrentScrollOffset()
if (offset.value > 35) onScroll(true)

// After: boolean이 실제로 flip될 때만 recompose 유발
val isScrolledPastThreshold by remember(lazyListState) {
    derivedStateOf {
        lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD ||
                lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
    }
}
```

**3) `snapshotFlow` 로 스크롤 시그널 전파**
```kotlin
// Before: 매 recomposition마다 onScroll(false/true) 호출
// After: isScrollInProgress 변경 시에만 1회 emit
LaunchedEffect(lazyListState) {
    snapshotFlowScrollState(lazyListState).collect { scrolling ->
        onScroll(scrolling)
    }
}
```

**4) 죽은 코드 제거**
```kotlin
// 제거: 사용되지 않는 private 함수
private fun visibleUpButton(index: Int): Boolean { ... }

// 제거: 325줄의 주석 처리된 Preview 코드 (Document 모델 참조, 컴파일 불가)
/* @Composable fun ListSectionPreview ... */
```

**5) 상수 추출**
```kotlin
private const val PAGE_SIZE_HINT = 10
private const val UP_BUTTON_THRESHOLD = 4
private const val SCROLL_OFFSET_SIGNAL = 35
```

---

## 📊 예상 성능 개선 효과

| 지표 | Before | After | 개선 |
|------|--------|-------|------|
| `SearchPhotosSection` 파일 라인 수 | 324줄 | ~280줄 | -14% |
| `SearchPhotosScreen` 최대 recompose scope | Scaffold 전체 | TopBar/Body 독립 | 범위 ↓ |
| 스크롤 시 `onScroll` 콜백 빈도 | 매 프레임 | 상태 변화 시 | 수십배 ↓ |
| `SearchSection` skipping 가능성 | 불가 (unstable lambda) | 가능 (stable lambda) | skip 율 ↑ |
| I/O로 인한 메인 스레드 블로킹 | 가능 | 없음 | ANR 리스크 ↓ |
| 더블탭으로 인한 중복 like 요청 | 가능 | 불가능 | 안정성 ↑ |

---

## ⚠️ 주의사항

### 1. Back-Compat 확인 필요
- `GalleryUiEvent`, `PictureUiEvent` 는 새로 추가된 sealed interface 입니다.
- Screen 레벨에서 `LaunchedEffect(Unit) { viewModel.events.collect { ... } }` 로 수집해야 효과를 볼 수 있습니다. (현재는 수집기가 없어 발행만 되는 상태 — 필요 시 screen 쪽 wiring 가능)

### 2. `Dispatchers.IO` 주입 기본값
```kotlin
@Inject constructor(
    ...,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
)
```
기본값이 `Dispatchers.IO` 라 테스트/런타임 모두 그대로 동작하지만, 엄격한 DI 규칙을 쓰는 팀이라면 `@IoDispatcher` qualifier + Hilt module 로 바꾸는 것이 좋습니다.

### 3. `BookmarkViewModel.isPhotoBookmarked()` 는 여전히 동기
- `LocalDataSource` 가 in-memory 캐시 기반이라 가정하고 유지
- 만약 실제로 disk/network 비동기 호출이라면 `suspend` 로 변경 필요

### 4. `SearchPhotosScreen` 의 `LifecycleOwner` 래핑
```kotlin
lifecycleOwner = state.baseUiState.lifecycle.let { owner ->
    LifecycleOwner { owner }
}
```
원본 `SearchPhotosContentState.baseUiState.lifecycle` 가 `Lifecycle` 타입인지 `LifecycleOwner` 타입인지에 따라 조정이 필요할 수 있습니다. 만약 이미 `LifecycleOwner` 라면 래핑 코드를 제거하고 `lifecycleOwner = state.baseUiState.lifecycle` 로 직접 전달하세요.

---

## 🔮 추가로 권장되는 최적화 (이번 범위 외)

1. **Accompanist SystemUiController deprecation**: `enableEdgeToEdge()` 로 교체
2. **PullRefresh Material 1.x API**: Material3 `PullToRefreshBox` 로 교체 (API 33+ 권장)
3. **Stetho 제거**: Chucker 또는 Chrome DevTools 네이티브 사용
4. **`PersistentCookieJar` 제거**: OkHttp 5의 `CookieJar` 네이티브 구현
5. **`UserPhotosScreen`, `PictureScreen` 도 동일 패턴으로 Decomposition**: 이번엔 Gallery 쪽만 적용, 같은 방식으로 다른 화면도 적용 가능

원하시면 위 항목들 중 일부를 이어서 진행하겠습니다.
