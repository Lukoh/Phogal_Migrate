package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class PopularPhotosSectionUiState(
    val clickedState: MutableState<Boolean>,
    val visibleUpButtonState: MutableState<Boolean>
)

@Composable
fun rememberPopularPhotosSectionUiState(
    clickedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PopularPhotosSectionUiState = remember(clickedState) {
    PopularPhotosSectionUiState(
        clickedState = clickedState,
        visibleUpButtonState = visibleUpButtonState
    )
}
