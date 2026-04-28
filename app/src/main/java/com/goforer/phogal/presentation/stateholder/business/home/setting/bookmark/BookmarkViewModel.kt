package com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.bookmark.BookmarkRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Exposes the list of locally-bookmarked pictures and lets the UI toggle bookmark state.
 *
 * All local-storage reads/writes are dispatched to [ioDispatcher] so the main thread
 * is never blocked. The StateFlow is a cached view of local storage — we refresh it
 * after every mutation so subscribers see the new value without re-querying.
 */
@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val localDataSource: LocalDataSource,
    bookmarkRepository: BookmarkRepository,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _bookmarkedPictures = MutableStateFlow<List<Picture>>(emptyList())
    val bookmarkedPictures: StateFlow<PagingData<Picture>> = bookmarkRepository
        .bookmarks(localDataSource.geBookmarkedPhotos()?.toMutableList()!!,  pageSize = PAGE_SIZE)
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    init {
        refresh()
    }

    /** Re-reads the bookmark list from local storage. */
    fun refresh() {
        viewModelScope.launch {
            _bookmarkedPictures.value = withContext(ioDispatcher) {
                localDataSource.geBookmarkedPhotos().orEmpty().toList()
            }
        }
    }

    /**
     * Adds [picture] to bookmarks and refreshes the flow.
     * I/O is dispatched off the main thread.
     */
    fun setBookmarkPicture(picture: Picture) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                localDataSource.setBookmarkPhoto(picture)
            }
            refresh()
        }
    }

    /**
     * Synchronous existence check. Cheap in-memory lookup; OK on main thread.
     * If the backing storage ever becomes truly async, convert this to `suspend`.
     */
    fun isPhotoBookmarked(picture: Picture): Boolean = localDataSource.isPhotoBookmarked(picture)

    /** Synchronous existence check by id — same rationale as above. */
    fun isPhotoBookmarked(id: String): Boolean = localDataSource.isPhotoBookmarked(id)

    companion object {
        const val PAGE_SIZE = 10
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
