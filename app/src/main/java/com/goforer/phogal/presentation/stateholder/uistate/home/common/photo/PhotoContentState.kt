package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

/**
 * UI-only holder for the single-picture screen.
 *
 * Unlike the legacy version, this class does not carry a mutable `picture: Picture?`
 * field — the picture now lives in `PictureViewModel.pictureUiState` as a proper
 * `StateFlow<UiState<Picture>>`. Keeping it out of this holder eliminates a source of
 * truth duplication that made the legacy `LikeResponseHandle` logic fragile.
 */
@Stable
class PhotoContentState(
    val baseUiState: BaseUiState,
    val idState: MutableState<String>,
    val visibleViewButtonState: MutableState<Boolean>,
    val enabledBookmarkState: MutableState<Boolean>,
    val visibleActionsState: MutableState<Boolean>
)

@Composable
fun rememberPhotoContentState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    idState: MutableState<String> = rememberSaveable { mutableStateOf("") },
    visibleViewButtonState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    enabledBookmarkState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleActionsState: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PhotoContentState = remember(
    baseUiState,
    idState,
    visibleViewButtonState
) {
    PhotoContentState(
        baseUiState = baseUiState,
        idState = idState,
        visibleViewButtonState = visibleViewButtonState,
        enabledBookmarkState = enabledBookmarkState,
        visibleActionsState = visibleActionsState
    )
}
