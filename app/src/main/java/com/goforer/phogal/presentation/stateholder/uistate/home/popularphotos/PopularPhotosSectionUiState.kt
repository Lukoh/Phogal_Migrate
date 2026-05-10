package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * UI-local state holder for the popular photos screen's scrollable section.
 *
 * ### Hoisting refactor (April 2026)
 *
 * Previously this holder exposed both flags as public `MutableState<Boolean>`,
 * which let any consumer write `state.clickedState.value = true` from anywhere
 * in the composable tree. The new shape exposes **read-only `Boolean`** values
 * and **typed callbacks** for the two transitions that actually happen:
 *
 *  - `onUpButtonClicked()`             — user tapped the "scroll-to-top" FAB
 *  - `onUpButtonVisibilityChanged(visible)` — visibility derived from scroll state
 *  - `onScrollConsumed()`              — clear the "click consumed" flag after scrollToTop animation
 *
 * Read-only properties (`clicked`, `visibleUpButton`) are stable from
 * Compose's perspective: the value flips, the holder identity does not, and
 * children that consume only one of them recompose only when that one
 * actually changes.
 */
@Stable
class PopularPhotosSectionUiState internal constructor(
    private val _clicked: MutableState<Boolean>,
    private val _visibleUpButton: MutableState<Boolean>,
    private val _loadingDone: MutableState<Boolean>
) {
    val clicked: Boolean get() = _clicked.value
    val visibleUpButton: Boolean get() = _visibleUpButton.value
    val loadingDone: Boolean get() = _loadingDone.value

    fun setUpButtonClicked() { _clicked.value = true }
    fun setScrollConsumed() { _clicked.value = false }
    fun setUpButtonVisibilityChanged(visible: Boolean) { _visibleUpButton.value = visible }

    fun setLoadingDone() { _loadingDone.value = true }
}

@Composable
fun rememberPopularPhotosSectionUiState(
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    loadingDone: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PopularPhotosSectionUiState = remember(clicked, visibleUpButton) {
    PopularPhotosSectionUiState(
        _clicked = clicked, _visibleUpButton = visibleUpButton, _loadingDone = loadingDone
    )
}
