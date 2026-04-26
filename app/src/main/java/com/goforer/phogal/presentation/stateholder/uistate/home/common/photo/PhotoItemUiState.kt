package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class PhotoItemUiState(
    val indexState:  MutableState<Int>,
    val photoState: State<Any>,
    val visibleViewButtonState: MutableState<Boolean>,
    val clickedState: MutableState<Boolean>,
    val bookmarkedState: MutableState<Boolean>
)

@Composable
fun rememberPhotoItemUiState(
    indexState: MutableState<Int> = rememberSaveable { mutableIntStateOf(0) },
    photoState: State<Any> = remember { mutableStateOf(Any()) },
    visibleViewButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    clickedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    bookmarkedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PhotoItemUiState = remember(indexState, photoState, visibleViewButtonState, clickedState) {
    PhotoItemUiState(
        indexState = indexState,
        photoState = photoState,
        visibleViewButtonState = visibleViewButtonState,
        clickedState = clickedState,
        bookmarkedState = bookmarkedState
    )
}