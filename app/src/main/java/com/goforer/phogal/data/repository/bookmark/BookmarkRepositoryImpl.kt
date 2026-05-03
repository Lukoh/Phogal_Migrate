package com.goforer.phogal.data.repository.bookmark

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.bookmark.paging.BookmarkPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor() : BookmarkRepository {
    override fun bookmarks(bookmarks: List<Picture>, pageSize: Int): Flow<PagingData<Picture>> {
        val bookmarkPagingFlow = Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { BookmarkPagingSource(bookmarks) }
        ).flow

        return bookmarkPagingFlow
    }
}