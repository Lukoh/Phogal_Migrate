package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class PhotoContentUiState internal constructor(
    val baseUiState: BaseUiState,

    private val _id: MutableState<String>,
    private val _visibleViewButton: MutableState<Boolean>,
    private val _enabledBookmark: MutableState<Boolean>,
    private val _visibleActions: MutableState<Boolean>
) {
    val id: String get() = _id.value
    val visibleViewButton: Boolean get() = _visibleViewButton.value
    val enabledBookmark: Boolean get() = _enabledBookmark.value
    val visibleActions: Boolean get() = _visibleActions.value

    fun onId(id: String) {
        _id.value = id
    }

    fun setVisibleViewButton(visibleViewButton: Boolean) {
        _visibleViewButton.value = visibleViewButton
    }

    fun setEnabledBookmark(enabledBookmark: Boolean) {
        _enabledBookmark.value = enabledBookmark
    }

    fun setVisibleActions(visibleActions: Boolean) {
        _visibleActions.value = visibleActions
    }
}

@Composable
fun rememberPhotoContentUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    id: MutableState<String> = rememberSaveable { mutableStateOf("") },
    visibleViewButton: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    enabledBookmark: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    visibleActions: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) }
): PhotoContentUiState = remember(baseUiState, id, visibleViewButton, enabledBookmark, visibleActions) {
        PhotoContentUiState(
            baseUiState = baseUiState,
            _id = id,
            _visibleViewButton = visibleViewButton,
            _enabledBookmark = enabledBookmark,
            _visibleActions = visibleActions
        )
    }
