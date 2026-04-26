package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
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
class SearchPhotosContentUiState(
    val baseUiState: BaseUiState,
    val galleryUiState: GalleryUiState,
    val enabledState: MutableState<Boolean>,
    val triggeredState: MutableState<Boolean>,
    val permissionState: MutableState<Boolean>,
    val rationaleTextState: MutableState<String>,
    val scrollingState: MutableState<Boolean>,
    val visibleActionsState: MutableState<Boolean>,
) {
    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
}

@Stable
class GalleryUiState(
    val photos: LazyPagingItems<Photo>,
    val currentQuery: String,
    val recentWords: List<String>
)

@Composable
fun rememberSearchPhotosContentUiState(
    galleryViewModel: GalleryViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    enabledState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    triggeredState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    permissionState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    rationaleTextState: MutableState<String> = rememberSaveable { mutableStateOf("") },
    scrollingState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleActionsState: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
): SearchPhotosContentUiState {
    val galleryUiState = rememberGalleryUiState(galleryViewModel)

    return remember(baseUiState, galleryUiState) {
        SearchPhotosContentUiState(
            baseUiState = baseUiState,
            galleryUiState = galleryUiState,
            enabledState = enabledState,
            triggeredState = triggeredState,
            permissionState = permissionState,
            rationaleTextState = rationaleTextState,
            scrollingState = scrollingState,
            visibleActionsState = visibleActionsState

        )
    }
}

@Composable
fun rememberGalleryUiState(
    galleryViewModel: GalleryViewModel
): GalleryUiState {
    // 1. 상태 수집
    val photos = galleryViewModel.photos.collectAsLazyPagingItems()
    val currentQuery by galleryViewModel.query.collectAsStateWithLifecycle()
    val recentWords by galleryViewModel.recentWords.collectAsStateWithLifecycle()

    // 2. 클래스에 담아서 반환
    return remember(photos, currentQuery, recentWords) {
        GalleryUiState(
            photos = photos,
            currentQuery = currentQuery,
            recentWords = recentWords
        )
    }
}