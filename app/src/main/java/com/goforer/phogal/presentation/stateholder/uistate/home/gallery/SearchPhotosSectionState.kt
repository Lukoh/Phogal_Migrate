package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * UI-local state for [com.goforer.phogal.presentation.ui.compose.screen.home.gallery.SearchPhotosSection].
 *
 * Paging data no longer lives here — the caller hands [LazyPagingItems] directly to
 * the section composable. This holder only tracks ephemeral UI flags.
 */
@Stable
class SearchPhotosSectionState(
    val clickedState: MutableState<Boolean>,
    val visibleUpButtonState: MutableState<Boolean>
)

@Composable
fun rememberSearchPhotosSectionState(
    clickedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): SearchPhotosSectionState = remember(clickedState) {
    SearchPhotosSectionState(
        clickedState = clickedState,
        visibleUpButtonState = visibleUpButtonState
    )
}
