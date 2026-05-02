package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class PopularPhotosContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val popularPhotosUiState: PopularPhotosUiState,

    private val _visibleActions: MutableState<Boolean>,
    private val _loadedPhotos: MutableState<Boolean>
) {
    val visibleActions: Boolean get() = _visibleActions.value
    val loadedPhotos: Boolean get() = _loadedPhotos.value

    fun setVisibleActions(visibleActions: Boolean) {
        _visibleActions.value = visibleActions
    }

    fun setLoadedPhotos(loadedPhotos: Boolean) {
        _loadedPhotos.value = loadedPhotos
    }
}

@Stable
class PopularPhotosUiState(
    val photos: LazyPagingItems<Photo>,
)

@Composable
fun rememberPopularPhotosContentUiState(
    popularPhotosViewModel: PopularPhotosViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    visibleActions: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    loadedPhotos: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
): PopularPhotosContentUiState {
    val popularPhotosUiState = rememberPopularPhotosUiState(popularPhotosViewModel)

    return remember(baseUiState, popularPhotosViewModel) {
        PopularPhotosContentUiState(
            baseUiState = baseUiState,
            popularPhotosUiState = popularPhotosUiState,
            _visibleActions = visibleActions,
            _loadedPhotos = loadedPhotos
        )
    }
}

@Composable
fun rememberPopularPhotosUiState(
    popularPhotosViewModel: PopularPhotosViewModel
): PopularPhotosUiState {
    val photos = popularPhotosViewModel.photos.collectAsLazyPagingItems()

    return remember(photos) {
        PopularPhotosUiState(
            photos = photos,
        )
    }
}
