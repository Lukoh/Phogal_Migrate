package com.goforer.phogal.presentation.stateholder.business.home.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.gallery.PhotosRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
    private val localDataSource: LocalDataSource,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _recentWords = MutableStateFlow<List<String>>(emptyList())
    val recentWords: StateFlow<List<String>> = localDataSource.searchWordsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // UI가 활성화될 때만 데이터 수집
            initialValue = emptyList()
        )

    /**
     * Stream of paged photos. Switches every time [query] changes (debounced, distinct).
     * Blank queries are filtered out so the UI's empty state doesn't burn a request.
     */
    val photos: StateFlow<PagingData<Photo>> = _query
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query -> photosRepository.search(query, PAGE_SIZE) }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    // ---------------- One-shot events ----------------

    private val _events = MutableSharedFlow<GalleryUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<GalleryUiEvent> = _events.asSharedFlow()

    init {
        refreshRecentWords()
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    /**
     * Commits the current query to local search history, capped at [MAX_HISTORY_SIZE].
     * I/O is dispatched off the main thread.
     */
    fun commitSearch() {
        val keyword = _query.value.trim()
        if (keyword.isEmpty()) return

        viewModelScope.launch {
            val updated = withContext(ioDispatcher) {
                val currentKeywords = recentWords.value.toMutableList()

                if (keyword in recentWords.value) return@withContext null
                if (currentKeywords.size >= MAX_HISTORY_SIZE) currentKeywords.removeAt(0)
                currentKeywords += keyword
                val snapshot = currentKeywords.toMutableList()
                localDataSource.setSearchWords(snapshot)
                snapshot
            } ?: return@launch

            _recentWords.value = updated
            _events.tryEmit(GalleryUiEvent.SearchCommitted(keyword))
        }
    }

    private fun refreshRecentWords() {
        viewModelScope.launch {
            _recentWords.value = withContext(ioDispatcher) {
                _recentWords.value.asReversed()
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val DEBOUNCE_MS = 300L
        const val MAX_HISTORY_SIZE = 7
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** One-shot UI events from [GalleryViewModel]. */
sealed interface GalleryUiEvent {
    data class SearchCommitted(val keyword: String) : GalleryUiEvent
}
