# Phogal — Navigation 3 Migration Notes

**Date:** 2026-04-19
**Target:** Navigation 3 **1.0.0 stable** (released November 2025)
**Compatible environment:** AGP 8.7.3 / Kotlin 2.0.21 / Gradle 8.10.2 / Android Studio Panda 2

---

## 🎯 Migration Strategy: Coexistence

This work introduces Nav3 **alongside** the existing Nav2 code — Nav2 is **not removed**.

```kotlin
// MainScreen.kt
private const val USE_NAV3: Boolean = false  // ← flip to true to route the entire app through Nav3
```

### Why run both in parallel?

1. **Nav3 has no nested-graph concept.** Today each of the four tabs wraps its routes in a `navigation<GalleryGraph>` block. Nav3 uses a single flat entry provider plus per-tab back-stack management. The structural change is large enough that an A/B toggle is the safe path for a production app.
2. **Multi-backstack must be implemented by hand.** What Nav2's `saveState=true`/`restoreState=true` did automatically — remembering each tab's last location across tab switches — is not provided by the framework in Nav3. We implemented it ourselves in `PhogalNavState`.
3. **ViewModel scope may shift.** Nav3's `rememberViewModelStoreNavEntryDecorator()` gives each entry its own `ViewModelStore`. Any ViewModel that previously relied on a shared nested-graph scope may behave differently, so on-device validation is required.

Both paths share the **same `Routes` definitions** (now implementing `NavKey`), so screen code, ViewModels, and repositories are untouched.

---

## 📁 Added/Modified Files

### Gradle configuration (2 files)
| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Added Nav3 version entries and library definitions |
| `app/build.gradle` | Added 3 Nav3 dependencies |

### Source code (5 files)
| File | Type | Role |
|------|------|------|
| `ui/navigation/Routes.kt` | Modified | Every route now implements `NavKey` |
| `ui/navigation/nav3/PhogalNavState.kt` | **New** | Per-tab back-stack manager |
| `ui/navigation/nav3/PhogalEntryProvider.kt` | **New** | Central registration of every screen's `entry<T>` |
| `ui/compose/screen/home/HomeScreenNav3.kt` | **New** | `NavDisplay`-based home screen |
| `stateholder/uistate/MainScreenStateNav3.kt` | **New** | Nav3-specific MainScreenState |
| `ui/compose/screen/MainScreen.kt` | Modified | Added the `USE_NAV3` toggle |

### Preserved Nav2 files (no changes)
- `ui/navigation/graph/GalleryNavGraph.kt`
- `ui/navigation/graph/PopularPhotosNavGraph.kt`
- `ui/navigation/graph/NotificationNavGraph.kt`
- `ui/navigation/graph/SettingNavGraph.kt`
- `ui/compose/screen/home/HomeScreen.kt`
- `stateholder/uistate/MainScreenState.kt`

---

## 🔍 Core API Mapping

| Concept | Nav2 | Nav3 |
|---------|------|------|
| Back-stack entry point | `rememberNavController()` | `rememberNavBackStack(...)` or manual management |
| Back-stack container | `NavHost(navController, startDestination)` | `NavDisplay(backStack, ...)` |
| Screen registration | `composable<Route> { }` | `entry<Route> { key -> }` |
| Forward navigation | `navController.navigate(Route(...))` | `backStack.add(Route(...))` |
| Back navigation | `navController.navigateUp()` | `backStack.removeLastOrNull()` |
| Parameter retrieval | `backStackEntry.toRoute<Route>()` | The `key` inside `entry<Route> { key -> }` *is* the data |
| Nested graphs | `navigation<Graph>(startDestination) { }` | **None** (manual handling) |
| Type safety | `@Serializable` | `@Serializable` + `NavKey` implementation |
| ViewModel scope | `hiltViewModel(backStackEntry)` | `hiltViewModel()` + `rememberViewModelStoreNavEntryDecorator()` |
| State restoration | Handled automatically by the framework | `rememberSavedStateNavEntryDecorator()` |

---

## 🧩 Design Notes

### 1. `PhogalNavState` — the multi-backstack manager

```kotlin
class PhogalNavState {
    private val stacks: Map<BottomNavDestination, SnapshotStateList<NavKey>>
    var currentTab: BottomNavDestination
    val backStackForCurrentTab: SnapshotStateList<NavKey>

    fun selectTab(tab: BottomNavDestination) {
        if (currentTab == tab) popToTabRoot()  // Tap the same tab again → pop to root
        else currentTab = tab                    // Different tab → keep its in-memory stack
    }

    fun push(key: NavKey) { backStackForCurrentTab.add(key) }
    fun pop(): Boolean { /* ... */ }
}
```

**Per-tab independent back-stack layout:**
- `stacks[Gallery]`: `[SearchPhotosRoute, PictureRoute("xyz"), UserPhotosRoute(...)]`
- `stacks[Popular]`: `[PopularPhotosRoute]`
- `stacks[Setting]`: `[SettingRoute, BookmarkedPhotosRoute]`

If the user moves Gallery → Setting and then back to Gallery, they resume at `UserPhotosRoute` — matching Nav2's `saveState/restoreState` UX exactly.

**Process-death handling:** a custom `Saver` persists and restores the state via `rememberSaveable`.

### 2. The three required `NavDisplay` decorators

```kotlin
NavDisplay(
    backStack = navState.backStackForCurrentTab,
    entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),      // Sets up animation scenes
        rememberSavedStateNavEntryDecorator(),      // Config-change resilience
        rememberViewModelStoreNavEntryDecorator()   // Hilt ViewModel scoping
    ),
    entryProvider = entryProvider { phogalEntries(navState) }
)
```

**What breaks if a decorator is missing:**
- Missing `rememberSceneSetupNavEntryDecorator` → predictive back-gesture animations break.
- Missing `rememberSavedStateNavEntryDecorator` → `rememberSaveable` state is lost on rotation.
- Missing `rememberViewModelStoreNavEntryDecorator` → multiple entries of the same type share a single ViewModel (serious bug).

### 3. Entry Provider structure

In Nav2 these registrations were split across four files (`GalleryNavGraph.kt`, `PopularPhotosNavGraph.kt`, …). In Nav3 we consolidated them into a single `phogalEntries()` function. Having the full route map in one place is easier to maintain than chasing it across files.

You can still split it up, e.g. into `GalleryEntries.kt`, `SettingEntries.kt`, each exposing an `EntryProviderBuilder<NavKey>` extension function, and then combine them:
```kotlin
entryProvider {
    galleryEntries(navState)
    popularEntries(navState)
    settingEntries(navState)
}
```
We chose the consolidated form here for simplicity.

---

## ⚠️ Cautions When Switching to Nav3

### 1. Shared scope for `BookmarkViewModel` / `FollowViewModel`

These ViewModels are used from both the Gallery tab and the Setting tab. Under Nav2, `@HiltViewModel` + `hiltViewModel()` hoisted them to Activity scope, so they were naturally shared.

With Nav3's `rememberViewModelStoreNavEntryDecorator()`, each **entry** gets its own `ViewModelStore`, meaning different instances of the same VM type unless you opt out.

**Fix:**
```kotlin
// Explicitly scope shared ViewModels to the Activity
val bookmarkVm: BookmarkViewModel = hiltViewModel(
    LocalContext.current as ViewModelStoreOwner
)
```

A cleaner alternative: keep state in a Hilt `@ActivityRetainedScoped` repository and let each ViewModel be short-lived while still reading the same underlying data.

### 2. `PopularPhotosScreen` and `NotificationsScreen` signatures

`PhogalEntryProvider.kt` currently assumes these signatures:
```kotlin
PopularPhotosScreen(onItemClicked = { id -> ... })
NotificationsScreen(onItemClicked = { id -> ... })
```

If the actual project signatures differ, compilation will fail. Check the original `PopularPhotosNavGraph.kt` and `NotificationNavGraph.kt` and match the parameters. (Copying from the existing Nav2 graph file is the quickest path.)

### 3. Deep links

Even in stable Nav3, the deep-link API differs from Nav2's. We have not confirmed whether deep-link configurations exist in this project; if they do, they need separate handling.

### 4. Back gestures

Nav3 supports Predictive Back out of the box. `NavDisplay(onBack = { count -> ... })` receives a `count` indicating how many steps to pop for a continuous back gesture.

Current implementation:
```kotlin
onBack = { count ->
    repeat(count) { navState.pop() }
}
```
When `pop()` returns `false` (already at root), it stops popping and `NavDisplay` propagates the event upward so the system can close the app.

---

## 🚀 Transition Testing Procedure

```bash
# 1. Build in the current state (Nav2)
./gradlew :app:assembleProdDebug
# → run the app and verify behavior

# 2. Change USE_NAV3 = true
#    (top of MainScreen.kt)

# 3. Build with Nav3
./gradlew :app:assembleProdDebug
# → run the app and compare:
#   - Each tab remembers its previous position when switching tabs
#   - Back navigation works correctly (including continuous back)
#   - State survives rotation
#   - Bookmark / Follow ViewModels share data ← watch this carefully
```

---

## 🔮 Next Steps

**Short term**
- Refactor `BookmarkViewModel`/`FollowViewModel` to an `@ActivityRetainedScoped` repository + lightweight VM structure.
- Adjust `entryProvider` so it matches the real signatures of `PopularPhotosScreen`/`NotificationsScreen`.
- After on-device validation, flip the `USE_NAV3` default to `true`.

**Medium term (Nav3-specific optimizations)**
- Adopt `ListDetailSceneStrategy` for automatic master-detail on tablets and foldables.
- Add Shared Element Transitions via `NavDisplay(sharedTransitionScope = ...)` for hero animations into the photo detail screen.
- Remove the Nav2 code (4 graph files + `HomeScreen.kt` + `MainScreenState.kt`).

**Long term**
- Deep-link handling (Nav3 style).
- Multiple navigation trees in a single Activity — e.g. a separate `NavDisplay` for the login flow.

---

## 📊 Expected Benefits

| Area | Benefit |
|------|---------|
| **Boilerplate** | Drops `NavController`, `NavGraphBuilder`, `backStackEntry.toRoute()` |
| **Type safety** | Stronger compile-time guarantees via `NavKey` |
| **Multi-backstack control** | Moves out of the framework black box — state is managed directly |
| **Adaptive layout** | Tablet support becomes a one-line `SceneStrategy` swap |
| **Compose-native feel** | State-driven, observable back stack |
| **Predictability** | Back navigation, scopes, and state restoration are all explicit |

---

If you have questions or run into errors registering a specific Nav3 entry, share the error log along with the offending file.
