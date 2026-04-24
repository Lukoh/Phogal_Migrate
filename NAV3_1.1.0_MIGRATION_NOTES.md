# Phogal — Navigation 3 **1.1.0** Migration Notes

**Date:** 2026-04-20
**Target:** **Navigation 3 1.1.0** (stable released 2026-04-08)
**Compatible environment:** AGP 8.7.3 / Kotlin 2.0.21 / Gradle 8.10.2 / Android Studio Panda 2

---

## 🎯 Summary

The project was already running on Nav3 1.0.0. This round aligns the internals with **1.1.0's new APIs and recommended patterns**, and fixes one latent bug discovered along the way.

### Three-line summary

1. **`rememberSavedStateNavEntryDecorator()` → `rememberSaveableStateHolderNavEntryDecorator()`**
2. **Dropped `rememberSceneSetupNavEntryDecorator()`** + **added `sceneStrategy = SinglePaneSceneStrategy()`**
3. **Made `transitionSpec` / `popTransitionSpec` / `predictivePopTransitionSpec` explicit**

---

## 📋 Changed Files (3)

| File | Type | What changed |
|------|------|--------------|
| `ui/navigation/nav3/PhogalNavState.kt` | Rewritten | Switched to the official `NavBackStack` type, built each tab via `rememberNavBackStack` |
| `ui/navigation/nav3/PhogalEntryProvider.kt` | Rewritten | Removed a bogus `import java.util.Map.entry` ⚠️, split by tab |
| `ui/compose/screen/home/HomeScreen.kt` | Rewritten | Fully updated to 1.1.0 APIs, extracted `DefaultTransitions` |

### Unchanged
- `libs.versions.toml` — version entries were already correct (`navigation3 = "1.1.0"`, `lifecycleViewmodelNavigation3 = "2.10.0"`)
- `Routes.kt` — `NavKey` implementations were already correct
- `MainScreenStateNav3.kt` — no change needed
- `MainScreen.kt` — no change needed

---

## 🔍 API Changes in Detail

### Change 1: Required decorator renamed (⚠️ breaking)

```kotlin
// ❌ 1.0.0 (old)
entryDecorators = listOf(
    rememberSceneSetupNavEntryDecorator(),      // Removed (now automatic)
    rememberSavedStateNavEntryDecorator(),      // ← renamed
    rememberViewModelStoreNavEntryDecorator()
)

// ✅ 1.1.0 (new)
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),   // ← new name
    rememberViewModelStoreNavEntryDecorator()
)
```

**Why the rename**: this decorator's actual job is to allocate a `SaveableStateHolder` to each entry so that `rememberSaveable` works at the entry level. The old "SavedState" name was easy to confuse with Android's `SavedStateRegistry`, so the new name reflects what the code actually does (use `SaveableStateHolder`).

### Change 2: `SceneSetup` decorator is now automatic

In 1.1.0, `rememberSceneSetupNavEntryDecorator()` is invoked internally by `sceneStrategy`. Adding it explicitly can cause it to run twice, and is no longer recommended.

### Change 3: `sceneStrategy` is now required

```kotlin
NavDisplay(
    backStack = backStack,
    sceneStrategy = remember { SinglePaneSceneStrategy<Any>() },  // ← new
    entryDecorators = [...],
    entryProvider = {...}
)
```

**`SinglePaneSceneStrategy`**: renders one screen at a time — the same behavior Nav2 always had.

**Extensibility**: when tablet/foldable support is needed later, this is a one-line change:
```kotlin
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
sceneStrategy = listDetailStrategy
```
On wide screens, list and detail render side-by-side; on narrow screens it falls back to push. You then add hints like `metadata = ListDetailSceneStrategy.listPane()` to the relevant `entry<T>` blocks and you're done.

### Change 4: Three explicit transition specs

1.1.0 distinguishes three transitions: `transitionSpec` (forward), `popTransitionSpec` (back), and `predictivePopTransitionSpec` (predictive back gesture).

```kotlin
NavDisplay(
    transitionSpec = DefaultTransitions.push,
    popTransitionSpec = DefaultTransitions.pop,
    predictivePopTransitionSpec = DefaultTransitions.predictivePop,
    ...
)
```

We parked each spec as a `val` inside a private `DefaultTransitions` object in `HomeScreen.kt`. They're created once and reused — no redundant lambda allocations.

### Change 5: Use the official `NavBackStack` type

```kotlin
// ❌ Hand-rolled during the 1.0.0 era
private val stacks: Map<Tab, SnapshotStateList<NavKey>>

// ✅ 1.1.0 recommended pattern
private val stacks: Map<Tab, NavBackStack>   // NavBackStack = SnapshotStateList<NavKey> + Saver
```

Calling `rememberNavBackStack(initialKey)` per tab gives each tab automatic process-death restoration. Previously we wrote a custom `Saver` inside `PhogalNavState`; now the framework does it for us.

---

## 🐛 Bug Discovered and Fixed

Line 28 of the uploaded `PhogalEntryProvider.kt` had this incorrect import:

```kotlin
import java.util.Map.entry   // ❌ java.util.Map.entry (static method)
```

When multiple symbols with the same name (`entry`) are in scope, Kotlin picks the "nearest" one, so depending on the situation `entry<Routes.SearchPhotosRoute> { ... }` could **be resolved to `java.util.Map.entry()` instead of the Nav3 DSL**, either causing a compile failure or — worse — passing in the IDE but misbehaving at runtime.

Corrected imports:
```kotlin
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry   // ✅ Nav3's real `entry` DSL
```

---

## ⚡ Compose Optimizations

### 1. `BottomNavBar` stability hardening

```kotlin
// ❌ Allocates a new list on every recomposition
val items = BottomNavDestination.values().toList()

// ✅ Computed once and reused (Kotlin 1.9+ `entries` API)
val items = remember { BottomNavDestination.entries }
```

### 2. Stabilizing the decorator list

```kotlin
val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator()
val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator()
val entryDecorators = remember(stateHolderDecorator, viewModelStoreDecorator) {
    listOf(stateHolderDecorator, viewModelStoreDecorator)
}
```

Each decorator is individually remembered during composition, but if their **containing list** is re-allocated every time, `NavDisplay`'s parameter equality check fails and triggers needless recompositions. Wrapping the list itself in `remember(decorators)` stabilizes it.

### 3. Reusing `SinglePaneSceneStrategy`

```kotlin
val sceneStrategy = remember { SinglePaneSceneStrategy<Any>() }
```

A `SceneStrategy` is a stateless strategy object — create it once.

### 4. `DefaultTransitions` object to hoist lambdas

The three transition lambdas are now class-level inside an `object`. This avoids capturing fresh lambdas on every `HomeScreen` recomposition and gives the JIT a clearer inlining path.

### 5. `bottomBarVisible` — dropped `animateDpAsState`

Previous code:
```kotlin
val bottomBarOffset by animateDpAsState(
    targetValue = if (bottomBarVisible) 0.dp else 80.dp,
    label = "bottomBarOffset"
)
```

1.1.0's `NavDisplay` already animates screen transitions via its own `transitionSpec`. Adding an `animateDpAsState` for the bottom bar on top of that creates a subtle mismatch between two different animation specs.

Fixed:
```kotlin
val bottomBarOffset: Dp = if (bottomBarVisible) 0.dp else 80.dp
```

The bottom bar now changes offset directly, and its visual transition rides along with the screen content's. If an explicit animation is needed, wrapping with `Modifier.animateContentSize()` or `AnimatedVisibility` aligns better with `NavDisplay`'s timing.

---

## 🔬 New in 1.1.0 — Available but Not Yet Used

### A. Shared Element Transitions (the 1.1.0 headline feature)

Hero animations between scenes — no more relying on Nav2-era Accompanist:

```kotlin
NavDisplay(
    sharedTransitionScope = rememberSharedTransitionScope(),
    ...
)

// Inside each entry:
Modifier.sharedElement(
    state = rememberSharedContentState(key = "photo_${photoId}"),
    animatedVisibilityScope = LocalNavAnimatedContentScope.current
)
```

Ready to apply to Phogal's thumbnail → detail-screen transition.

### B. `ListDetailSceneStrategy` — tablet/foldable support

```kotlin
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

entry<Routes.SearchPhotosRoute>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { /* "Pick a photo" UI */ }
    )
) { ... }

entry<Routes.PictureRoute>(
    metadata = ListDetailSceneStrategy.detailPane()
) { key -> ... }
```

On large screens, list and detail are visible simultaneously. On small screens, it falls back to the existing push behavior.

### C. Dialog destinations

```kotlin
@Serializable data class SomeDialog(val id: String) : NavKey

entry<SomeDialog>(
    metadata = DialogSceneStrategy.dialog()
) { ... }

NavDisplay(
    sceneStrategy = listOf(
        SinglePaneSceneStrategy(),
        DialogSceneStrategy()
    ),
    ...
)
```

Dialogs become back-stack citizens too: back-stack restoration, deep links, and predictive back all work automatically.

---

## ✅ Build Verification

```bash
./gradlew :app:assembleProdDebug
```

Compile-only check:
```bash
./gradlew :app:compileProdDebugKotlin
```

Expected first-build warnings/errors and their fixes:

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Unresolved reference: rememberSavedStateNavEntryDecorator` | Old API still referenced elsewhere | Project-wide replace with `rememberSaveableStateHolderNavEntryDecorator` |
| `Unresolved reference: rememberSceneSetupNavEntryDecorator` | Removed in 1.1.0 | Delete that line from the list |
| `NavDisplay` parameter mismatch | Using the 1.0.0 signature | Add the `sceneStrategy` parameter |
| `BottomNavDestination.values()` warning | Kotlin 1.9+ recommends `entries` | Use `.entries` |

---

## 📦 Compose Stability Checklist

Compose best practices applied in this round:

- [x] Collected transition specs into a `@Stable` object — no re-allocation
- [x] Stabilized the decorator list with `remember`
- [x] Used `BottomNavDestination.entries` (avoids unnecessary list allocation)
- [x] Removed the duplicate `animateDpAsState` animation
- [x] Switched to the official `NavBackStack` type — no custom `rememberSaveable` needed
- [x] Split `phogalEntries` into four private functions → reduces full-file recomposition
- [x] Removed the bogus `java.util.Map.entry` import

---

## 🔮 Recommended Next Steps

**Short term (this sprint)**
- On-device testing of the changes above — especially tab-switch back-stack preservation.
- Verify the predictive back-gesture animation UX (Android 14+).

**Medium term (next sprint)**
- Apply Shared Element Transitions to the thumbnail → detail transition.
- Use `ListDetailSceneStrategy` for tablet landscape mode.

**Long term (refactoring pass)**
- Fully remove the Nav2 dependency (if `navigation-compose` 2.8.x is still in the build).
- Bring bottom sheets and dialogs into navigation via `DialogSceneStrategy`.

---

## 📊 Before / After

| Item | Before (Nav3 1.0.0 code) | After (Nav3 1.1.0 code) |
|------|--------------------------|--------------------------|
| Decorator count | 3 (includes SceneSetup) | 2 (automatic ones removed) |
| Decorator name correctness | Old-name decorator | 1.1.0 accurate names |
| `sceneStrategy` parameter | Absent | `SinglePaneSceneStrategy` |
| Transition spec | Defaults | All three explicit (push/pop/predictive) |
| Back-stack type | `SnapshotStateList<NavKey>` | `NavBackStack` (official) |
| Back-stack persistence | Custom `Saver` | `rememberNavBackStack` (automatic) |
| Erroneous import | Yes (`java.util.Map.entry`) | Removed |
| BottomNavBar lambda stability | New list every recomposition | Stabilized via `remember` |
| Bottom bar animation | `animateDpAsState` (possible mismatch) | Synchronized with `NavDisplay` transitions |

If you hit build errors, share the full error log. Nav3 is much more stable than during the alpha days, but transient compiler errors are usually about decorator names or imports.
