package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class UserPhotosSectionUiState internal constructor(
    private val _clicked: MutableState<Boolean>,
    private val _visibleUpButton: MutableState<Boolean>
) {
    val clicked: Boolean get() = _clicked.value
    val visibleUpButton: Boolean get() = _visibleUpButton.value

    fun setUpButtonClicked(clicked: Boolean) {
        _clicked.value = clicked
    }

    fun setScrollConsumed(clicked: Boolean) {
        _clicked.value = clicked
    }
    fun setUpButtonVisibilityChanged(
        visible: Boolean
    ) {
        _visibleUpButton.value = visible
    }
}

@Composable
fun rememberUserPhotosSectionUiState(
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): UserPhotosSectionUiState = remember(clicked, visibleUpButton) {
        UserPhotosSectionUiState(_clicked = clicked, _visibleUpButton = visibleUpButton)
    }
