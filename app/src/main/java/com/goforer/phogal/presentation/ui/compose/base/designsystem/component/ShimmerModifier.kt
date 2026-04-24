package com.goforer.phogal.presentation.ui.compose.base.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * An in-house shimmer placeholder modifier that replaces the discontinued
 * **Accompanist placeholder** library (`com.google.accompanist:accompanist-placeholder-material`),
 * which was retired by Google in 2024.
 *
 * Why we rolled our own rather than switch to a fork:
 *  - Zero new external dependencies — one fewer thing to track during future
 *    Compose BOM upgrades.
 *  - Short enough to audit (<60 lines) and tweak to match our own motion spec.
 *  - The old Accompanist call sites used a large surface of options we never
 *    actually exercised; distilling to the one shape we need keeps call sites
 *    readable.
 *
 * ## Visual spec (matches the Accompanist default we were using)
 *  - Base: a flat surface color the consumer passes in (commonly
 *    [MaterialTheme.colorScheme] for light sections or
 *    `ColorSystemGray7` for the photo grid).
 *  - Highlight: a diagonal linear gradient of `baseColor → highlightColor → baseColor`
 *    that sweeps from the top-left to the bottom-right of the composable
 *    infinitely, producing the familiar "metal sheen" effect.
 *  - Duration per sweep: ~1,200 ms, reversing so the motion never resets
 *    abruptly.
 *
 * ## Usage
 * ```
 * if (isLoading) {
 *     Box(
 *         Modifier
 *             .fillMaxWidth()
 *             .height(256.dp)
 *             .shimmer(baseColor = ColorSystemGray7)
 *     )
 * }
 * ```
 *
 * ## Migration from Accompanist (reference)
 * Old:
 * ```
 * .placeholder(
 *     visible = true,
 *     color = MaterialTheme.colorScheme.surface,
 *     highlight = PlaceholderHighlight.shimmer(...)
 * )
 * ```
 * New:
 * ```
 * .shimmer(baseColor = MaterialTheme.colorScheme.surface)
 * ```
 *
 * The old Accompanist `.placeholder(visible = ...)` also gated rendering on
 * a boolean. With this modifier, the call site does the gating with a simple
 * `if (isLoading) { ... }` wrapper — which, in practice, is what we already do.
 */
@Composable
fun Modifier.shimmer(
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color = MaterialTheme.colorScheme.surface,
    durationMillis: Int = 1_200,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer-progress",
    )

    // Compose the base fill first, then overlay the moving gradient using a
    // drawWithContent pattern. A linear gradient whose start/end points travel
    // across the bounds produces the sheen.
    this
        .background(baseColor)
        .graphicsLayer { }                // establishes a draw layer so the
                                          // brush below composites cleanly
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    baseColor,
                    highlightColor,
                    baseColor,
                ),
                // The gradient's end-point slides with `progress`, sweeping the
                // highlight across the element. Using a pair of float coords
                // (x, y) of equal magnitude keeps the sweep diagonal.
                start = Offset(
                    x = progress * 1_000f - 500f,
                    y = progress * 1_000f - 500f,
                ),
                end = Offset(
                    x = progress * 1_000f - 500f + 400f,
                    y = progress * 1_000f - 500f + 400f,
                ),
            )
        )
}

/**
 * A convenience overload that hard-codes `MaterialTheme.colorScheme.surface`
 * as the highlight colour — this matches what every previous call site passed.
 *
 * Keep [shimmer] if you need a custom highlight; prefer this when the defaults
 * are fine (which is almost always).
 */
@Composable
@Suppress("unused")
fun Modifier.shimmerWithDefaults(): Modifier =
    shimmer(
        baseColor = MaterialTheme.colorScheme.surfaceVariant,
        highlightColor = MaterialTheme.colorScheme.surface,
    )
