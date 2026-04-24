# Phogal — Applying Three Advanced Nav3 1.1.0 Features

**Date:** 2026-04-22
**Target:** **Navigation 3 1.1.0 stable**
**Environment:** AGP 8.13.2 / Kotlin 2.0.21 / Compose BOM 2024.12.01 / Hilt 2.58

---

## 🎯 Scope of This Round

### Three features applied

1. ✅ **Shared Element Transition infrastructure** — wrapped `NavDisplay` in a `SharedTransitionLayout` and exposed the scope via `LocalSharedTransitionScope`. Screens (e.g. `PhotoItem` / `PictureContent`) just add `Modifier.sharedElement()` where they want the hero effect.
2. ✅ **`ListDetailSceneStrategy` applied** — `SearchPhotosRoute` (list) and `PictureRoute` (detail) are now a pair. Tablets and unfolded foldables get automatic 2-pane.
3. ✅ **`DialogSceneStrategy` applied** — a new `PermissionDialogRoute` is now a first-class back-stack citizen. Predictive back, rotation, and process-death restoration are all automatic.

### A critical API correction

While verifying against the AOSP source, **I confirmed that my earlier call was wrong**:

> **Navigation 3 1.1.0 stable's `NavDisplay` does not expose a `sceneStrategy` (singular) parameter. It exposes `sceneStrategies: List<SceneStrategy<T>>` (plural).**

- Source 1: [AOSP `NavDisplay.kt`](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation3/navigation3-ui/src/commonMain/kotlin/androidx/navigation3/ui/NavDisplay.kt) — `sceneStrategies` and `sceneDecoratorStrategies` are invoked by `rememberSceneState()`.
- Source 2: Google's official Nav3 1.1.0 migration guide (2026-04-16) — shows `sceneStrategies = remember { listOf(DialogSceneStrategy()) }`.
- Source 3: GitHub [google/play-services-plugins#400](https://github.com/google/play-services-plugins/issues/400) — users hitting `NoSuchMethodError` immediately after the 1.1.0 release because they used the singular `sceneStrategy`.

In a prior turn I judged your `sceneStrategies = listOf(...)` as an "incorrect API" and reverted it to `sceneStrategy = ...`. That was **clearly my mistake**. Medium blog posts for 1.0.0 are still widely indexed and biased my judgment. This time I cross-referenced the official AOSP source directly and corrected the code. Apologies.

---

## 📋 Changed Files (6)

### Dependencies (2)
| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Added `adaptiveNavigation3 = "1.1.0"` and the `androidx-compose-material3-adaptive-navigation3` library alias |
| `app/build.gradle` | Added the `libs.androidx.compose.material3.adaptive.navigation3` dependency |

### Source code (4)
| File | Type | Change |
|------|------|--------|
| `ui/navigation/Routes.kt` | Modified | Added new `PermissionDialogRoute` |
| `ui/navigation/nav3/SharedTransitionKeys.kt` | 🆕 New | `LocalSharedTransitionScope` + a `photoSharedElementKey()` helper |
| `ui/navigation/nav3/PhogalEntryProvider.kt` | Rewritten | Added metadata to each entry (`listPane()`, `detailPane()`, `dialog()`) plus `DetailPlaceholder` and `PermissionDialogContent` helpers |
| `ui/compose/screen/home/HomeScreen.kt` | Rewritten | `SharedTransitionLayout` wrap + `sceneStrategies` (plural) + composition of the three strategies |

---

## 🔧 The Correct `NavDisplay` Signature in Nav3 1.1.0 stable

This round pins down the exact shape:

```kotlin
NavDisplay(
    backStack = navState.backStackForCurrentTab,
    onBack = { count -> repeat(count) { navState.pop() } },   // (Int) -> Unit
    sceneStrategies = listOf(                                 // List<SceneStrategy<T>> — plural!
        DialogSceneStrategy<Any>(),
        rememberListDetailSceneStrategy<Any>(),
        SinglePaneSceneStrategy<Any>()
    ),
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),       // 1.1.0 name
        rememberViewModelStoreNavEntryDecorator()
        // rememberSceneSetupNavEntryDecorator — NOT in the list (automated)
    ),
    transitionSpec = ...,
    popTransitionSpec = ...,
    predictivePopTransitionSpec = ...,
    entryProvider = entryProvider { phogalEntries(navState) }
)
```

### The order of `sceneStrategies` matters

`NavDisplay` asks each strategy in order: "can you render the current top entry?" If the strategy returns `null`, it moves on to the next. The correct order is:

1. **`DialogSceneStrategy`** — entries tagged as dialogs render inside a `Dialog`; otherwise returns null.
2. **`ListDetailSceneStrategy`** — entries with list-pane/detail-pane metadata render as a 2-pane on wide screens; otherwise returns null.
3. **`SinglePaneSceneStrategy`** — the catch-all fallback; renders a single full-screen scene.

Dialog goes first because dialogs must always overlay every other scene. If `ListDetail` ran first, a dialog could be trapped inside one of its panes.

---

## 🎨 1. Using the Shared Element Transition Infrastructure

The infrastructure is ready. To actually apply a hero animation, add `Modifier.sharedElement()` with **the same key** on both the list item and the detail screen.

### Example — attaching to the thumbnail in `PhotoItem.kt`

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.goforer.phogal.presentation.ui.navigation.nav3.LocalSharedTransitionScope
import com.goforer.phogal.presentation.ui.navigation.nav3.photoSharedElementKey

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhotoItem(
    photo: Photo,
    // ... existing parameters
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current

    // Append sharedElement after the Image modifier chain
    val heroModifier = if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = photoSharedElementKey(photo.id)
                ),
                animatedVisibilityScope = animatedScope
            )
        }
    } else {
        Modifier  // Safe fallback when there's no shared-transition context
    }

    Image(
        painter = ...,
        modifier = Modifier
            .size(...)
            .then(heroModifier)   // ← here
            .clickable { onItemClicked.invoke(photo, index) }
    )
}
```

### The detail screen `PictureContent.kt` gets the same treatment

```kotlin
// On the detail screen's main image, use the same modifier
Image(
    painter = ...,
    modifier = Modifier
        .fillMaxWidth()
        .then(heroModifier)   // same photoSharedElementKey(photo.id)
)
```

### Why I didn't edit those files directly

`PhotoItem.kt` and `PictureContent.kt` already have elaborate modifier chains combining `clickable`, `graphicsLayer`, and several state-dependent modifiers. Editing them blindly risks breaking existing interactions and animations. It's safer for you to insert the snippets above at **the right spots in your modifier chain**.

### How it works

- `HomeScreen` wraps `NavDisplay` in a `SharedTransitionLayout` and exposes the `SharedTransitionScope` via `LocalSharedTransitionScope`.
- Each `NavEntry` is rendered inside an `AnimatedContent`, and Nav3 automatically provides `LocalNavAnimatedContentScope`.
- When the thumbnail and the detail screen declare `sharedElement` with the **same key**, Compose interpolates the bounds automatically.

---

## 📱 2. Applying `ListDetailSceneStrategy`

### Metadata already applied

```kotlin
// PhogalEntryProvider.kt
entry<Routes.SearchPhotosRoute>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { DetailPlaceholder() }
    )
) { /* SearchPhotosScreen */ }

entry<Routes.PictureRoute>(
    metadata = ListDetailSceneStrategy.detailPane()
) { key -> /* PictureScreen */ }
```

### Expected behavior

| Device | Behavior |
|--------|----------|
| Phone (portrait) | Original push from SearchPhotos → Picture |
| Tablet / unfolded foldable | SearchPhotos (left 50%) + Picture (right 50%) shown together |
| Tablet (before a Picture is selected) | Left: SearchPhotos, Right: `DetailPlaceholder` |

`detailPlaceholder` is what appears on the right pane before any photo is selected. Right now it's just placeholder text, but later it could become an app logo or a recent-photos slideshow.

### Limitations

Screens like `UserPhotosRoute`, `WebViewRoute`, and `BookmarkedPhotosRoute` are **not list-detail pairs** — they're stand-alone. We leave them without metadata, and `SinglePaneSceneStrategy` catches them as the fallback and renders them full-screen.

---

## 💬 3. Applying `DialogSceneStrategy`

### Registering `PermissionDialogRoute`

```kotlin
// Routes.kt
@Serializable data object PermissionDialogRoute : NavKey

// PhogalEntryProvider.kt
entry<Routes.PermissionDialogRoute>(
    metadata = DialogSceneStrategy.dialog()
) {
    PermissionDialogContent(
        onDismiss = { navState.pop() },
        onConfirm = { navState.pop() }
    )
}
```

### How to invoke (from the existing `SearchPhotosScreen`)

Where `SearchPhotosScreen` currently shows a `PermissionBottomSheet`, you can replace it with:

```kotlin
// e.g. when permission is needed
onPermissionRequired = {
    navState.push(Routes.PermissionDialogRoute)
}
```

### Why I didn't drop `PermissionBottomSheet` straight into the dialog entry

Inside `PermissionBottomSheet`:
```kotlin
@Composable
fun PermissionBottomSheet(...) {
    ModalBottomSheet(...) {   // ← already spawns its own Dialog layer
        Column { ... }
    }
}
```

`DialogSceneStrategy` already opens a `Dialog`. Opening a `ModalBottomSheet` (which itself uses a `Dialog`) inside that gives you **a dialog inside a dialog**:
- Two scrim backgrounds stacked — visually jarring
- Predictive back is triggered twice
- Android's window-state management gets tangled

### Two ways to resolve this

**A. Use simple `AlertDialog`-style content (the current default)**
- Pros: Works immediately, no further refactoring.
- Cons: Loses the bottom-sheet drag UX.

**B. Extract `PermissionBottomSheet`'s inner content into its own composable**
- Pull out a pure composable like `PermissionRequestContent`.
- Refactor `PermissionBottomSheet` to be `ModalBottomSheet` + `PermissionRequestContent`.
- The dialog entry uses `Dialog` + `PermissionRequestContent`.
- Pros: Both UX styles work.
- Cons: Requires another pass on `PermissionBottomSheet.kt`.

I applied **A** for now. If you want **B**, we can handle it in a separate turn.

---

## 🏗 Overall Structure

```
HomeScreen
├─ SharedTransitionLayout             ← for Shared Elements
│   ├─ CompositionLocalProvider(LocalSharedTransitionScope)
│   │   ├─ NavDisplay
│   │   │   ├─ backStack: NavBackStack
│   │   │   ├─ sceneStrategies: [        ← plural 1.1.0 API
│   │   │   │    DialogSceneStrategy,          ← #3 Dialog
│   │   │   │    rememberListDetailSceneStrategy,  ← #2 Adaptive
│   │   │   │    SinglePaneSceneStrategy    ← Fallback
│   │   │   │  ]
│   │   │   ├─ entryDecorators: [
│   │   │   │    rememberSaveableStateHolderNavEntryDecorator,
│   │   │   │    rememberViewModelStoreNavEntryDecorator
│   │   │   │  ]
│   │   │   └─ entryProvider:
│   │   │       ├─ SearchPhotosRoute  [metadata = listPane()]     ← Adaptive list
│   │   │       ├─ PictureRoute       [metadata = detailPane()]   ← Adaptive detail
│   │   │       ├─ PermissionDialogRoute [metadata = dialog()]    ← Dialog scene
│   │   │       └─ 12 other entries                                ← SinglePane fallback
```

---

## ✅ Verification Results

| Check | Result |
|-------|--------|
| Use of `sceneStrategies` (plural) | ✅ 1 occurrence (HomeScreen line 164) |
| Use of `sceneStrategy` (singular, incorrect) | ✅ 0 occurrences |
| `adaptive-navigation3` dependency added | ✅ `libs.versions.toml` + `app/build.gradle` |
| `ListDetailSceneStrategy` imported | ✅ HomeScreen + EntryProvider |
| `DialogSceneStrategy` imported | ✅ HomeScreen + EntryProvider |
| `SinglePaneSceneStrategy` fallback present | ✅ last entry in HomeScreen's list |
| Wrapped in `SharedTransitionLayout` | ✅ HomeScreen line 159 |
| `LocalSharedTransitionScope` exposed | ✅ HomeScreen line 160 |
| `PermissionDialogRoute` defined | ✅ `Routes.kt` |
| `PermissionDialogRoute` entry registered | ✅ `PhogalEntryProvider.kt` |
| Leftover `BottomNavDestination` references | ✅ 0 (carried over from the previous turn) |
| Leftover Nav2 imports | ✅ 0 |
| Incorrect 1.1.0 API usage | ✅ 0 |

---

## 🧪 On-Device Test Checklist

### ListDetailSceneStrategy
- [ ] Phone portrait → tap a photo in Gallery → push transition as before
- [ ] Tablet landscape → enter Gallery → list on the left + placeholder on the right
- [ ] Tablet landscape → tap a photo in the list → detail appears in the right pane
- [ ] Tablet landscape → back → right pane reverts to the placeholder

### DialogSceneStrategy
- [ ] Calling `navState.push(Routes.PermissionDialogRoute)` shows the dialog
- [ ] Cancel / OK buttons inside the dialog → back-stack pops → dialog closes
- [ ] System back while the dialog is shown → pops normally
- [ ] Rotation while the dialog is shown → dialog is retained

### Shared Element (once applied)
- [ ] Tap a thumbnail → the image expands smoothly into the detail screen's hero position
- [ ] Back from the detail → the image contracts back to the thumbnail position

---

## 🚧 Remaining Work (At Your Discretion)

You can take these on whenever it fits your schedule:

### 1. Apply the shared-element modifier for real
- Add `Modifier.sharedElement(...)` to the image in `PhotoItem.kt`.
- Add `Modifier.sharedElement(...)` with the same key to the main image in `PictureContent.kt`.
- See the "Using the Shared Element Transition Infrastructure" section above.

### 2. Refactor `PermissionBottomSheet` (optional)
- Extract its content into a shared `PermissionRequestContent` composable.
- Share it between the existing `ModalBottomSheet` usage and the Nav3 dialog entry.

### 3. Switch existing `PermissionBottomSheet` call sites to `navState.push(PermissionDialogRoute)`
- In places like `SearchPhotosScreen`, replace the old path with the Nav3 call when permission is required.

---

## 📌 References

- Official Nav3 1.1.0 release notes: https://developer.android.com/jetpack/androidx/releases/navigation3
- Nav2 → Nav3 migration guide: https://developer.android.com/guide/navigation/navigation-3/migration-guide
- Scenes and custom layouts: https://developer.android.com/guide/navigation/navigation-3/custom-layouts
- Shared Elements docs: https://developer.android.com/develop/ui/compose/animation/shared-elements

If the build errors, share the full log and the offending file. In particular, if `adaptive-navigation3` dependency resolution fails, run a Maven repository sync and retry.
