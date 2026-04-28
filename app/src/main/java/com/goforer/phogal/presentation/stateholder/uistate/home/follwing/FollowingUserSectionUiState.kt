package com.goforer.phogal.presentation.stateholder.uistate.home.follwing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

class FollowingUserSectionUiState(
    val clickedState: MutableState<Boolean>,
    val visibleUpButtonState: MutableState<Boolean>
)

@Composable
fun rememberFollowingUserSectionUiState(
    clickedState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): FollowingUserSectionUiState = remember(clickedState) {
    FollowingUserSectionUiState(
        clickedState = clickedState,
        visibleUpButtonState = visibleUpButtonState
    )
}