package com.goforer.phogal.data.repository.common.photo.info

import com.goforer.phogal.BuildConfig
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PictureRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : PictureRepository {

    override suspend fun getPicture(id: String): NetworkResult<Picture> = safeApiCall {
        api.getPhoto(id = id, clientId = BuildConfig.clientId)
    }
}
