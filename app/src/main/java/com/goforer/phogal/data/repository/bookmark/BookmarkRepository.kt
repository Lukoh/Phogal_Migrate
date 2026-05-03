package com.goforer.phogal.data.repository.bookmark

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    /**
     * @param bookmarks     Bookmarked the photolist (non-blank)
     * @param pageSize  page size for [androidx.paging.PagingConfig]; also used as initial load size
     */
    fun bookmarks(bookmarks: List<Picture>, pageSize: Int): Flow<PagingData<Picture>>
}