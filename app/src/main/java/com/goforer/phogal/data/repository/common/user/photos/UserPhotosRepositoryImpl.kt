package com.goforer.phogal.data.repository.common.user.photos

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.data.repository.common.user.photos.paging.UserPhotosPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPhotosRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : UserPhotosRepository {

    override fun userPhotos(username: String, pageSize: Int): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                prefetchDistance = (pageSize - 5).coerceAtLeast(1),
                enablePlaceholders = false
            ),
            pagingSourceFactory = { UserPhotosPagingSource(api, username, pageSize) }
        ).flow
    }
}
