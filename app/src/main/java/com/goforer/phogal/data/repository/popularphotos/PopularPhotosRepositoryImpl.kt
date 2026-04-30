package com.goforer.phogal.data.repository.popularphotos

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.popularphotos.paging.PopularPhotosPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PopularPhotosRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : PopularPhotosRepository {

    override fun popularPhotos(orderBy: String, pageSize: Int): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                prefetchDistance = (pageSize - 5).coerceAtLeast(1),
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularPhotosPagingSource(api, orderBy, pageSize) }
        ).flow
    }
}
