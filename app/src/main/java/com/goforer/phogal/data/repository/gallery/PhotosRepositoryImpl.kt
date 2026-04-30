package com.goforer.phogal.data.repository.gallery

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.gallery.paging.PhotosPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotosRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : PhotosRepository {

    override fun search(query: String, pageSize: Int): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                prefetchDistance = (pageSize - 5).coerceAtLeast(1),
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PhotosPagingSource(api, query, pageSize) }
        ).flow
    }
}
