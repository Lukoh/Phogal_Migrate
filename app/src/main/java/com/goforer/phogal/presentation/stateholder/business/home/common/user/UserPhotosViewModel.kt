package com.goforer.phogal.presentation.stateholder.business.home.common.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for "photos by user" screen.
 *
 * Username is treated as state (via [loadFor]) so the same VM instance can serve
 * multiple users without re-creation. `distinctUntilChanged` prevents re-triggering
 * a Pager when the same username is set twice (e.g. on config change + restore).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserPhotosViewModel @Inject constructor(
    private val userPhotosRepository: UserPhotosRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")

    val photos: StateFlow<PagingData<Photo>> = _username
        .flatMapLatest { username ->
            if (username.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                userPhotosRepository.userPhotos(username, PAGE_SIZE)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    /** Sets (or resets) which user we're viewing. Safe to call multiple times. */
    fun loadFor(username: String) {
        _username.value = username
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
