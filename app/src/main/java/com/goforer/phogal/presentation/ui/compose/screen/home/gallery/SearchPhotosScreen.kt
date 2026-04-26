package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.goforer.base.designsystem.component.CardSnackBar
import com.goforer.base.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.base.designsystem.component.ScaffoldContent
import com.goforer.phogal.R
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchSectionUiState
import com.goforer.phogal.presentation.ui.theme.ColorBgSecondary
import com.goforer.phogal.presentation.ui.theme.PhogalTheme
import kotlinx.coroutines.launch

/**
 * Top-level screen for photo search.
 *
 * Responsibilities are split across three private composables:
 *  - [SearchTopBar]        — title + menu/favorite actions
 *  - [SearchSnackbarHost]  — branded snackbar host
 *  - [ObserveLifecycle]    — side-effect for ON_START / ON_STOP callbacks
 *
 * Keeping them separated means each can recompose independently and each is
 * trivially previewable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPhotosScreen(
    modifier: Modifier = Modifier,
    galleryViewModel: GalleryViewModel,
    contentUiState: SearchPhotosContentUiState = rememberSearchPhotosContentUiState(galleryViewModel),
    sectionUiState: SearchSectionUiState = rememberSearchSectionUiState(enabledState = contentUiState.enabledState),
    onItemClicked: (id: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Stable lambdas — created once per VM instance so child composables
    // don't see a new reference on every recomposition.
    val onSearch: (String) -> Unit = remember(galleryViewModel, contentUiState.galleryUiState.currentQuery, contentUiState) {
        { keyword ->
            if (keyword.isNotEmpty() && keyword != contentUiState.galleryUiState.currentQuery) {
                contentUiState.baseUiState.keyboardController?.hide()
                galleryViewModel.onQueryChanged(keyword)
                galleryViewModel.commitSearch()
                contentUiState.triggeredState.value = true
            }
        }
    }

    val onChipClicked: (String) -> Unit = remember(galleryViewModel, contentUiState, sectionUiState) {
        { keyword ->
            sectionUiState.editableInputState.textState = keyword
            contentUiState.baseUiState.keyboardController?.hide()
            galleryViewModel.onQueryChanged(keyword)
            galleryViewModel.commitSearch()
        }
    }

    ObserveLifecycle(
        lifecycleOwner = LocalLifecycleOwner.current,
        onStart = onStart,
        onStop = onStop
    )

    BackHandler(enabled = true) {
        (contentUiState.baseUiState.context as Activity).finish()
    }

    Scaffold(
        contentColor = ColorBgSecondary,
        snackbarHost = { SearchSnackbarHost(snackbarHostState) },
        topBar = {
            SearchTopBar(
                showFavoriteAction = contentUiState.visibleActionsState.value,
                onMenuClick = { /* TODO */ },
                onFavoriteClick = { /* TODO */ }
            )
        },
        content = { paddingValues ->
            ScaffoldContent(topInterval = 8.dp) {
                SearchPhotosContent(
                    modifier = modifier.padding(
                        top = paddingValues.calculateTopPadding()
                    ),
                    photosContentUiState = contentUiState,
                    onSearch = onSearch,
                    onChipClicked = onChipClicked,
                    onItemClicked = onItemClicked,
                    onViewPhotos = onViewPhotos,
                    onShowSnackBar = { message ->
                        contentUiState.baseUiState.scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onOpenWebView = onOpenWebView,
                    onSuccess = { contentUiState.visibleActionsState.value = it }
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Extracted composables
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    showFavoriteAction: Boolean,
    onMenuClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    CustomCenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.bottom_navigation_gallery),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Profile")
            }
        },
        actions = {
            if (showFavoriteAction) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorites"
                    )
                }
            }
        }
    )
}

@Composable
private fun SearchSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
        snackbar = { snackbarData: SnackbarData ->
            CardSnackBar(modifier = Modifier, snackbarData)
        }
    )
}

/**
 * Side-effect wrapper that invokes [onStart] on ON_START and [onStop] on ON_STOP.
 * Uses `rememberUpdatedState` so the lambdas captured by the observer always
 * point at the latest version, not the one from the first composition.
 */
@Composable
private fun ObserveLifecycle(
    lifecycleOwner: LifecycleOwner,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart()
                Lifecycle.Event.ON_STOP -> currentOnStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPhotosScreenPreview() {
    PhogalTheme {
        Scaffold(
            contentColor = Color.White,
            topBar = {
                SearchTopBar(
                    showFavoriteAction = true,
                    onMenuClick = {},
                    onFavoriteClick = {}
                )
            }
        ) { /* preview body */ }
    }
}
