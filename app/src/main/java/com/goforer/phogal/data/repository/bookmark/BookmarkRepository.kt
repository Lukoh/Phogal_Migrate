package com.goforer.phogal.data.repository.bookmark

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.coroutines.flow.Flow

/**
 * Bookmark photos on Unsplash
 *
 * Consumers should typically call [bookmarks] from a ViewModel-scoped coroutine and
 * forward the resulting [Flow] through `cachedIn(viewModelScope)` before exposing it
 * to the UI layer as a `StateFlow<PagingData<Picture>>`.
 */
interface BookmarkRepository {
    /**
     * @param bookmarks     Bookmarked the photolist (non-blank)
     * @param pageSize  page size for [androidx.paging.PagingConfig]; also used as initial load size
     */
    fun bookmarks(bookmarks: List<Picture>, pageSize: Int): Flow<PagingData<Picture>>
}