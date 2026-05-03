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
