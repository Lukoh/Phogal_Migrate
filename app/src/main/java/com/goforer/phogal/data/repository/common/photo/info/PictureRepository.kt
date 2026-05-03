package com.goforer.phogal.data.repository.common.photo.info

import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture

interface PictureRepository {
    /**
     * @param id Unsplash photo id
     * @return a [NetworkResult] wrapping the [Picture] on success
     */
    suspend fun getPicture(id: String): NetworkResult<Picture>
}
