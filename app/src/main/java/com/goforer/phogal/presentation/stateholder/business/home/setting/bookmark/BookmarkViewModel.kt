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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val localDataSource: LocalDataSource,
    bookmarkRepository: BookmarkRepository,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _bookmarkedPictures = MutableStateFlow<List<Picture>>(emptyList())

    val photos: StateFlow<List<Picture>> = localDataSource.bookmarkedPhotosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // UI가 활성화될 때만 데이터 수집
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarkedPictures: StateFlow<PagingData<Picture>> = photos
        .flatMapLatest { photos ->
            bookmarkRepository.bookmarks(photos.toMutableList(), pageSize = PAGE_SIZE)
        }
        .cachedIn(viewModelScope) // 페이징 상태 유지
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
                localDataSource.bookmarkedPhotosFlow
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()).value.toMutableList().toList()
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
                localDataSource.toggleBookmarkPhoto(picture)
            }

            refresh()
        }
    }

    /**
     * Synchronous existence check. Cheap in-memory lookup; OK on main thread.
     * If the backing storage ever becomes truly async, convert this to `suspend`.
     */
    fun isPhotoBookmarked(picture: Picture): Boolean = localDataSource.isPhotoBookmarkedFlow(picture).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value

    /** Synchronous existence check by id — same rationale as above. */
    fun isPhotoBookmarked(id: String): Boolean = localDataSource.isPhotoBookmarkedFlow(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value

    companion object {
        const val PAGE_SIZE = 10
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
