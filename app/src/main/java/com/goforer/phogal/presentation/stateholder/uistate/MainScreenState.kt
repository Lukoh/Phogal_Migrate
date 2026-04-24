package com.goforer.phogal.presentation.stateholder.uistate

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.goforer.base.utils.connect.NetworkMonitor
import com.goforer.phogal.presentation.ui.compose.screen.home.BottomNavRoute
import com.goforer.phogal.presentation.ui.navigation.nav3.PhogalNavState
import com.goforer.phogal.presentation.ui.navigation.nav3.rememberPhogalNavState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Top-level screen state for Phogal.
 *
 * Nav3 1.1.0 edition — there is no `NavHostController`. Navigation state lives
 * entirely inside [navState] ([PhogalNavState]). `currentTopLevelDestination`
 * is read synchronously from that state — no `@Composable get()` required
 * because the underlying value is plain Compose state, not a suspend/observable.
 */
@Stable
class MainScreenState(
    val navState: PhogalNavState,
    val coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor
) {
    val currentTopLevelDestination: BottomNavRoute
        get() = navState.currentTab

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = false
        )
}

@Composable
fun rememberMainScreenState(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navState: PhogalNavState = rememberPhogalNavState(initialTab = BottomNavRoute.Gallery)
): MainScreenState = remember(navState, coroutineScope, windowSizeClass, networkMonitor) {
    MainScreenState(
        navState = navState,
        coroutineScope = coroutineScope,
        windowSizeClass = windowSizeClass,
        networkMonitor = networkMonitor
    )
}
