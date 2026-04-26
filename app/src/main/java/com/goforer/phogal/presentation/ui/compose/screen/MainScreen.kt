package com.goforer.phogal.presentation.ui.compose.screen

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.base.designsystem.component.Background
import com.goforer.base.designsystem.component.GradientBackground
import com.goforer.base.designsystem.theme.GradientColors
import com.goforer.base.designsystem.theme.LocalGradientColors
import com.goforer.base.utils.connect.ConnectivityManagerNetworkMonitor
import com.goforer.phogal.presentation.stateholder.uistate.rememberMainScreenUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.BottomNavRoute
import com.goforer.phogal.presentation.ui.compose.screen.home.HomeScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.OfflineScreen

/**
 * App entry composable. This project uses **Navigation 3 1.1.0 exclusively**
 * (Nav2 has been fully removed). All navigation state is owned by
 * [com.goforer.phogal.presentation.ui.navigation.nav3.NavigationState], threaded
 * through [com.goforer.phogal.presentation.stateholder.uistate.MainScreenUiState].
 */
@Composable
fun MainScreen(
    networkMonitor: ConnectivityManagerNetworkMonitor,
    windowSizeClass: WindowSizeClass
) {
    val state = rememberMainScreenUiState(
        windowSizeClass = windowSizeClass,
        networkMonitor = networkMonitor
    )
    val shouldShowGradientBackground =
        state.currentTopLevelDestination == BottomNavRoute.Gallery

    Background {
        GradientBackground(
            gradientColors = if (shouldShowGradientBackground) {
                LocalGradientColors.current
            } else {
                GradientColors()
            }
        ) {
            val isOffline by state.isOffline.collectAsStateWithLifecycle()

            if (isOffline) {
                OfflineScreen(modifier = Modifier)
            } else {
                HomeScreen(
                    modifier = Modifier,
                    shouldShowBottomBar = state.shouldShowBottomBar,
                    navigationState = state.navState
                )
            }
        }
    }
}
