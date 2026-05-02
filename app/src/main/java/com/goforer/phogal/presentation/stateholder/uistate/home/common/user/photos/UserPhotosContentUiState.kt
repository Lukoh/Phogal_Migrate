package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class UserPhotosContentUiState internal constructor(
    val baseUiState: BaseUiState,

    private val _name: MutableState<String>,
    private val _firstName: MutableState<String>,
    private val _visibleActions: MutableState<Boolean>
) {
    val name: String get() = _name.value
    val firstName: String get() = _firstName.value
    val visibleActions: Boolean get() = _visibleActions.value

    fun setName(name: String) {
        _name.value = name
    }

    fun setFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun setVisibleAction(visibleActions: Boolean) {
        _visibleActions.value = visibleActions
    }
}

@Composable
fun rememberUserPhotosContentUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    name: MutableState<String> = rememberSaveable { mutableStateOf("") },
    firstName: MutableState<String> = rememberSaveable { mutableStateOf("") },
    visibleActions: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): UserPhotosContentUiState = remember(
        name,
        firstName,
        visibleActions
    ) {
        UserPhotosContentUiState(
            baseUiState = baseUiState,
            _name = name,
            _firstName = firstName,
            _visibleActions = visibleActions
        )
    }
