package com.goforer.phogal.presentation.stateholder.business.home.common.photo.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.common.photo.info.PictureRepository
import com.goforer.phogal.data.repository.common.photo.like.PictureLikeRepository
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.toUiStateStrict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PictureViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
    private val pictureLikeRepository: PictureLikeRepository
) : ViewModel() {

    private val _pictureUiState = MutableStateFlow<UiState<Picture>>(UiState.Idle)
    val pictureUiState: StateFlow<UiState<Picture>> = _pictureUiState.asStateFlow()

    private val _likeActionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val likeActionState: StateFlow<UiState<Unit>> = _likeActionState.asStateFlow()

    private val _events = MutableSharedFlow<PictureUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<PictureUiEvent> = _events.asSharedFlow()

    /**
     * Fetches a picture by id. Idempotent — calling twice with the same id re-runs
     * the request and replaces state.
     */
    fun loadPicture(id: String) {
        if (id.isBlank()) return
        _pictureUiState.value = UiState.Loading
        viewModelScope.launch {
            _pictureUiState.value = pictureRepository.getPicture(id).toUiStateStrict()
        }
    }

    /**
     * Toggles like on the currently loaded picture.
     *
     * Guards:
     *  - No-op unless a picture is successfully loaded.
     *  - No-op if a like action is already in flight (prevents rapid double-tap).
     *
     * On success, patches the in-memory Picture atomically via [update].
     */
    fun toggleLike() {
        val current = (_pictureUiState.value as? UiState.Success)?.data ?: return
        if (_likeActionState.value is UiState.Loading) return

        val wasLiked = current.likedByUser
        val pictureId = current.id

        _likeActionState.value = UiState.Loading
        viewModelScope.launch {
            val result = if (wasLiked) {
                pictureLikeRepository.unlike(pictureId)
            } else {
                pictureLikeRepository.like(pictureId)
            }

            when (result) {
                is NetworkResult.Success, NetworkResult.Empty -> {
                    // Atomic patch of the nested state — avoids races with a concurrent loadPicture.
                    _pictureUiState.update { state ->
                        if (state is UiState.Success && state.data.id == pictureId) {
                            UiState.Success(state.data.copy(likedByUser = !wasLiked))
                        } else state
                    }
                    _likeActionState.value = UiState.Success(Unit)
                    _events.tryEmit(PictureUiEvent.LikeToggled(liked = !wasLiked))
                }
                is NetworkResult.Error -> {
                    _likeActionState.value = UiState.Error(code = result.code, message = result.message)
                    _events.tryEmit(PictureUiEvent.LikeFailed(result.message))
                }
                is NetworkResult.Exception -> {
                    val msg = result.throwable.message ?: "Network failure"
                    _likeActionState.value = UiState.Error(code = 0, message = msg)
                    _events.tryEmit(PictureUiEvent.LikeFailed(msg))
                }
            }
        }
    }

    /** Resets the transient like-action state back to Idle after the UI shows its feedback. */
    fun consumeLikeAction() {
        _likeActionState.value = UiState.Idle
    }
}

/** One-shot UI events from [PictureViewModel]. */
sealed interface PictureUiEvent {
    data class LikeToggled(val liked: Boolean) : PictureUiEvent
    data class LikeFailed(val message: String) : PictureUiEvent
}
