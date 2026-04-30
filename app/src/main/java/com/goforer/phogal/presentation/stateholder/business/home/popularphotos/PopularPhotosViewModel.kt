package com.goforer.phogal.presentation.stateholder.business.home.popularphotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the popular-photos feed. No param switching — the feed is static
 * from the client's perspective — so the repository stream is created eagerly and
 * held as a single [StateFlow].
 */
@HiltViewModel
class PopularPhotosViewModel @Inject constructor(
    popularPhotosRepository: PopularPhotosRepository
) : ViewModel() {
    /*
    val photos: StateFlow<PagingData<Photo>> = popularPhotosRepository
        .popularPhotos(orderBy = POPULAR, pageSize = PAGE_SIZE)
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

     */

    private val _orderBy = MutableStateFlow(POPULAR)
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val photos: StateFlow<PagingData<Photo>> = _orderBy
        .flatMapLatest { popularPhotosRepository.popularPhotos(orderBy = POPULAR, pageSize = PAGE_SIZE) }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    fun updateOrderBy(newOrderBy: String) {
        _orderBy.value = newOrderBy
    }

    companion object {
        const val POPULAR = "popular"
        const val LATEST = "latest"
        const val OLDEST = "oldest"
        const val PAGE_SIZE = 10
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
