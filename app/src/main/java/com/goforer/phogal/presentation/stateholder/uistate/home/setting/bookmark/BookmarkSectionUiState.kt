package com.goforer.phogal.presentation.stateholder.uistate.home.setting.bookmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class BookmarkSectionUiState(
    val clickedState: MutableState<Boolean>,
    val visibleUpButtonState: MutableState<Boolean>
)

@Composable
fun rememberBookmarkSectionUiState(
    clickedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): BookmarkSectionUiState = remember(clickedState) {
    BookmarkSectionUiState(
        clickedState = clickedState,
        visibleUpButtonState = visibleUpButtonState
    )
}
