package com.goforer.phogal.presentation.ui.navigation.nav3

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal that exposes the `SharedTransitionScope` from the
 * `SharedTransitionLayout` that wraps our `NavDisplay`. Any descendant
 * composable can read this to build `Modifier.sharedElement(...)` without
 * having to thread the scope through every composable signature.
 *
 * For the paired `AnimatedContentScope` — which is the second argument every
 * shared element modifier also needs — use the Nav3-provided
 * `androidx.navigation3.ui.LocalNavAnimatedContentScope`. Nav3 1.1.0 sets
 * that local automatically inside every `NavEntry` content slot, so you do
 * **not** need to define a custom version of it.
 *
 * ## Usage (inside any screen composable)
 *
 * ```
 * val sharedScope = LocalSharedTransitionScope.current ?: return
 * val animatedScope = LocalNavAnimatedContentScope.current
 *
 * with(sharedScope) {
 *     Image(
 *         painter = ...,
 *         modifier = Modifier.sharedElement(
 *             sharedContentState = rememberSharedContentState(
 *                 key = photoSharedElementKey(photoId)
 *             ),
 *             animatedVisibilityScope = animatedScope
 *         )
 *     )
 * }
 * ```
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * Builds a stable, collision-free shared element key for a photo thumbnail ↔
 * detail pair. The photo `id` uniquely identifies the element within the app.
 *
 * Using a typed helper (instead of string concatenation scattered across
 * files) keeps the contract between the list screen and the detail screen
 * in one place — if the key scheme ever changes, there is only one file
 * to update.
 */
fun photoSharedElementKey(photoId: String): String = "photo-hero-$photoId"
