package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

/**
 * UI-only holder for the gallery search screen.
 *
 * The legacy version carried `photosUiState: StateFlow<Any>` and `refreshingState:
 * StateFlow<Boolean>` directly. Both are gone: the screen now reads Paging state from
 * `GalleryViewModel.photos.collectAsLazyPagingItems()` and derives refresh state from
 * `lazyPagingItems.loadState.refresh`. That's the canonical Paging3 pattern.
 */
@Stable
class SearchPhotosContentState(
    val baseUiState: BaseUiState,
    val enabledState: MutableState<Boolean>,
    val triggeredState: MutableState<Boolean>,
    val permissionState: MutableState<Boolean>,
    val rationaleTextState: MutableState<String>,
    val scrollingState: MutableState<Boolean>,
    val visibleActionsState: MutableState<Boolean>
) {
    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
}

@Composable
fun rememberSearchPhotosContentState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    enabledState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    triggeredState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    permissionState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    rationaleTextState: MutableState<String> = rememberSaveable { mutableStateOf("") },
    scrollingState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleActionsState: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) }
): SearchPhotosContentState = remember(baseUiState) {
    SearchPhotosContentState(
        baseUiState = baseUiState,
        enabledState = enabledState,
        triggeredState = triggeredState,
        permissionState = permissionState,
        rationaleTextState = rationaleTextState,
        scrollingState = scrollingState,
        visibleActionsState = visibleActionsState
    )
}
