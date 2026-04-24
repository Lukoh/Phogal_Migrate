package com.goforer.phogal.data.repository.common.photo.like

import com.goforer.phogal.BuildConfig
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.photo.like.LikeResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PictureLikeRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : PictureLikeRepository {

    override suspend fun like(pictureId: String): NetworkResult<LikeResponse> = safeApiCall {
        api.postLike(id = pictureId, clientId = BuildConfig.clientId)
    }

    override suspend fun unlike(pictureId: String): NetworkResult<LikeResponse> = safeApiCall {
        api.deleteLike(id = pictureId, clientId = BuildConfig.clientId)
    }
}
