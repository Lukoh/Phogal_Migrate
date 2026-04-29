package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class PopularPhotosContentUiState(
    val baseUiState: BaseUiState,
    val popularPhotosUiState: PopularPhotosUiState,
    val visibleActionsState: MutableState<Boolean>,
    val loadedPhotosState: MutableState<Boolean>
)

@Stable
class PopularPhotosUiState(
    val photos: LazyPagingItems<Photo>,
)

@Composable
fun rememberPopularPhotosContentUiState(
    popularPhotosViewModel: PopularPhotosViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    visibleActionsState: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    loadedPhotosState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
): PopularPhotosContentUiState {
    val popularPhotosUiState = rememberPopularPhotosUiState(popularPhotosViewModel)

    return remember(baseUiState, popularPhotosViewModel) {
        PopularPhotosContentUiState(
            baseUiState = baseUiState,
            popularPhotosUiState = popularPhotosUiState,
            visibleActionsState = visibleActionsState,
            loadedPhotosState = loadedPhotosState
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
