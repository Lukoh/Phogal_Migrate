package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class PopularPhotosContentState(
    val baseUiState: BaseUiState,
    val visibleActionsState: MutableState<Boolean>,
    val loadedPhotosState: MutableState<Boolean>
)

@Composable
fun rememberPopularPhotosContentState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    visibleActionsState: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    loadedPhotosState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
): PopularPhotosContentState = remember(baseUiState) {
    PopularPhotosContentState(
        baseUiState = baseUiState,
        visibleActionsState = visibleActionsState,
        loadedPhotosState = loadedPhotosState
    )
}
