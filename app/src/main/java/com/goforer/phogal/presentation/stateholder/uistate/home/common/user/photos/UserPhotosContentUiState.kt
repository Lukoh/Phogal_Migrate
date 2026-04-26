package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

/**
 * UI-only holder for the "user photos" screen. Paging data is now read directly from
 * `UserPhotosViewModel.photos` via `collectAsLazyPagingItems()` in the screen.
 */
@Stable
class UserPhotosContentUiState(
    val baseUiState: BaseUiState,
    val nameState: MutableState<String>,
    val firstNameState: MutableState<String>,
    val visibleActionsState: MutableState<Boolean>
)

@Composable
fun rememberUserPhotosContentUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    nameState: MutableState<String> = rememberSaveable { mutableStateOf("") },
    firstNameState: MutableState<String> = rememberSaveable { mutableStateOf("") },
    visibleActionsState: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) }
): UserPhotosContentUiState = remember(
    baseUiState,
    nameState,
    firstNameState
) {
    UserPhotosContentUiState(
        baseUiState = baseUiState,
        nameState = nameState,
        firstNameState = firstNameState,
        visibleActionsState = visibleActionsState
    )
}
