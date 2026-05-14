package com.goforer.phogal.presentation.ui.compose.screen.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.goforer.phogal.presentation.ui.navigation.nav3.LocalSharedTransitionScope
import com.goforer.phogal.presentation.ui.navigation.nav3.NavigationState
import com.goforer.phogal.presentation.ui.navigation.nav3.phogalEntries
import com.goforer.phogal.presentation.ui.navigation.nav3.rememberNavigationState

import com.goforer.phogal.presentation.ui.theme.Blue80
import com.goforer.phogal.presentation.ui.theme.ColorBgSecondary
import com.goforer.phogal.presentation.ui.theme.ColorBottomBar

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    shouldShowBottomBar: Boolean,
    navigationState: NavigationState = rememberNavigationState()
) {
    // Bottom bar visibility flows straight from state — no listener needed.
    val bottomBarVisible = !navigationState.canPopInCurrentRoute

    // M3 NavigationBar default height is 80.dp. We translate off-screen when hidden.
    val bottomBarOffset: Dp = if (bottomBarVisible) 0.dp else 80.dp

    // All strategies + decorators parameterized as <NavKey> so that
    // NavDisplay<NavKey> ↔ entryProvider<NavKey> ↔ phogalEntries(EntryProviderScope<NavKey>)
    // share the same type parameter end-to-end.
    //
    // Order: Dialog first (overlays) → ListDetail (adaptive) → SinglePane (fallback).
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val sceneStrategies = remember(listDetailStrategy) {
        listOf(
            DialogSceneStrategy(),
            listDetailStrategy,
            SinglePaneSceneStrategy()
        )
    }

    val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val entryDecorators = remember(stateHolderDecorator, viewModelStoreDecorator) {
        listOf(stateHolderDecorator, viewModelStoreDecorator)
    }

    Scaffold(
        modifier = modifier,
        containerColor = ColorBgSecondary,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavBar(
                    currentRoute = navigationState.currentRoute,
                    visible = bottomBarVisible,
                    offset = bottomBarOffset,
                    onTabSelected = navigationState::selectRoute
                )
            }
        },
        content = { innerPadding ->
            Box(
                Modifier.padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = if (bottomBarVisible) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                // Wrap NavDisplay in a SharedTransitionLayout and pipe its scope into
                // a CompositionLocal so any descendant can do shared-element animations.
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        /*
                        // Please unblock this code if androidx.navigation3.runtime.1.1.1 could be applied...
                        NavDisplay(
                            backStack = navigationState.backStackForCurrentRoute,
                            onBack = { navigationState.pop() },
                            sceneStrategies = sceneStrategies,
                            entryDecorators = entryDecorators,
                            transitionSpec = DefaultTransitions.push, // 타입이 일치함
                            popTransitionSpec = DefaultTransitions.pop,
                            predictivePopTransitionSpec = DefaultTransitions.predictivePop,
                            entryProvider = entryProvider { phogalEntries(navigationState) }
                        )

                         */

                        NavDisplay(
                            backStack = navigationState.backStackForCurrentRoute,
                            onBack = { navigationState.pop() },
                            entryDecorators = entryDecorators,
                            transitionSpec = DefaultTransitions.push,
                            popTransitionSpec = DefaultTransitions.pop,
                            sceneStrategy = sceneStrategies.firstOrNull() as SceneStrategy<NavKey>,
                            predictivePopTransitionSpec = DefaultTransitions.predictivePop,
                            entryProvider = entryProvider { phogalEntries(navigationState) }
                        )
                    }
                }
            }
        }
    )
}

// ─────────────────────────── Transition specs (extracted) ───────────────────────────

/**
 * Default transition specs for the app's main [NavDisplay].
 *
 * Nav3 1.1.0 declares each spec as an **extension function on the
 * animated-content scope**:
 *
 *   `transitionSpec: AnimatedContentTransitionScope<NavEntry<T>>.() -> ContentTransform`
 *
 * Inside each spec body we can therefore call `slideIntoContainer`,
 * `slideOutOfContainer`, `fadeIn`, etc. directly — the
 * `AnimatedContentTransitionScope` is the implicit receiver (`this`).
 *
 * A previous revision used the form `(scope) -> ContentTransform` and called
 * methods via the explicit `scope` parameter. That signature does NOT match
 * the `NavDisplay` parameter type, so the compiler produced a cascade of
 * misleading type errors — including the receiver-mismatch error this file
 * was submitted to fix. This revision uses the correct extension form.
 *
 * Using `*` star projection for the receiver lets us share the same lambda
 * across NavDisplay regardless of the concrete `T` — the animation only
 * touches scope methods that don't depend on `T`.
 */
@Stable
private object DefaultTransitions {
    private const val DURATION_MS = 300
    private const val PREDICTIVE_DURATION_MS = 250

    /** Push (forward) */
    // .() -> 를 .(Int) -> 로 수정하고, 람다 블록에 { _ -> 를 추가합니다.
    val push: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION_MS)
        ) + fadeIn(tween(DURATION_MS))
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION_MS)
        ) + fadeOut(tween(DURATION_MS))
        enter togetherWith exit
    }

    /** Pop (backward) */
    val pop: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION_MS)
        ) + fadeIn(tween(DURATION_MS))
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION_MS)
        ) + fadeOut(tween(DURATION_MS))
        enter togetherWith exit
    }


    /** Predictive back */
    val predictivePop: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { _ ->
        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(PREDICTIVE_DURATION_MS)
        ) + fadeIn(tween(PREDICTIVE_DURATION_MS))
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(PREDICTIVE_DURATION_MS)
        ) + fadeOut(tween(PREDICTIVE_DURATION_MS))
        enter togetherWith exit
    }
}

// ─────────────────────────── Bottom bar (extracted) ───────────────────────────

@Composable
private fun BottomNavBar(
    currentRoute: BottomNavRoute,
    visible: Boolean,
    offset: Dp,
    onTabSelected: (BottomNavRoute) -> Unit
) {
    val items = remember { BottomNavRoute.entries }

    NavigationBar(
        containerColor = ColorBottomBar,
        contentColor = Blue80,
        tonalElevation = 5.dp,
        modifier = if (visible) {
            Modifier.navigationBarsPadding()
        } else {
            Modifier.offset { IntOffset(x = 0, y = offset.value.toInt()) }
        }
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(id = item.title)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.title),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                selected = currentRoute == item,
                alwaysShowLabel = false,
                onClick = { onTabSelected(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue80,
                    selectedTextColor = Blue80,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
