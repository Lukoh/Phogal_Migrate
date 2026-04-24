package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goforer.phogal.presentation.ui.theme.Blue70

/**
 * "Scroll to top" floating action button.
 *
 * Migrated from Material 2 `androidx.compose.material.FloatingActionButton`
 * to Material 3 `androidx.compose.material3.FloatingActionButton`. Two
 * deliberate differences in Material 3:
 *
 *  - The color parameter is `containerColor` (not `backgroundColor`), since
 *    Material 3 separates container, content, and elevation-overlay colors.
 *  - No default tonal elevation mismatch to worry about — M3 FAB already
 *    draws the correct Material You elevation surface.
 */
@Composable
fun ShowUpButton(modifier: Modifier, visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier
    ) {
        FloatingActionButton(
            modifier = modifier
                .padding(bottom = 8.dp, end = 8.dp),
            containerColor = Blue70,
            onClick = onClick
        ) {
            Text("Up!")
        }
    }
}