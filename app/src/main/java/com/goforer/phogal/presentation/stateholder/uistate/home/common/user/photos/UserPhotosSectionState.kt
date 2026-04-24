package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class UserPhotosSectionState(
    val clickedState: MutableState<Boolean>,
    val visibleUpButtonState: MutableState<Boolean>
)

@Composable
fun rememberUserPhotosSectionState(
    clickedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): UserPhotosSectionState = remember(clickedState) {
    UserPhotosSectionState(
        clickedState = clickedState,
        visibleUpButtonState = visibleUpButtonState
    )
}
