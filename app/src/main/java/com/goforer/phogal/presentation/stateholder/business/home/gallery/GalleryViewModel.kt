package com.goforer.phogal.presentation.stateholder.business.home.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.data.repository.gallery.PhotosRepository
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

/**
 * ViewModel for the gallery search screen.
 *
 * ## State (observed by UI)
 *  - [query]       : current text in the search box (writable via [onQueryChanged])
 *  - [photos]      : paginated results, derived reactively from [query]
 *  - [recentWords] : search history (local-storage backed)
 *
 * ## Events (one-shot, not replayed)
 *  - [events] : transient signals like "search committed" — use this for navigation,
 *               snackbars, keyboard dismissal. State flows are for persistent data;
 *               events are for things that happen once and shouldn't re-fire on rotation.
 *
 * ## Design notes
 *  - I/O (LocalDataSource) is dispatched to [ioDispatcher] so the main thread isn't blocked.
 *  - [recentWords] is backed by a MutableStateFlow that is updated explicitly rather than
 *    re-read on every commit — local storage is the source of truth, the flow is a cache.
 *  - The repository is only invoked when [query] is non-blank.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
    private val localDataSource: LocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // ---------------- State ----------------

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _recentWords = MutableStateFlow<List<String>>(emptyList())
    val recentWords: StateFlow<List<String>> = _recentWords.asStateFlow()

    /**
     * Stream of paged photos. Switches every time [query] changes (debounced, distinct).
     * Blank queries are filtered out so the UI's empty state doesn't burn a request.
     */
    val photos: StateFlow<PagingData<Photo>> = _query
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { q -> photosRepository.search(q, PAGE_SIZE) }
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

    // ---------------- Intents ----------------

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
                val current = localDataSource.getSearchWords().orEmpty().toMutableList()
                if (keyword in current) return@withContext null

                if (current.size >= MAX_HISTORY_SIZE) current.removeAt(0)
                current += keyword
                val snapshot = current.toList()
                localDataSource.setSearchWords(snapshot)
                snapshot
            } ?: return@launch

            _recentWords.value = updated.asReversed()
            _events.tryEmit(GalleryUiEvent.SearchCommitted(keyword))
        }
    }

    fun selectRecentWord(word: String) {
        _query.value = word
    }

    private fun refreshRecentWords() {
        viewModelScope.launch {
            _recentWords.value = withContext(ioDispatcher) {
                localDataSource.getSearchWords()?.asReversed().orEmpty()
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
