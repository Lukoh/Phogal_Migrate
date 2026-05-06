package com.goforer.phogal.data.model.remote.response.gallery.photos

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class PhotosResponse(
    val results: MutableList<Photo>,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int
) : Parcelable {
    companion object {
        fun empty() = PhotosResponse(
            results = mutableListOf(),
            total = 0,
            totalPages = 0
        )
    }
}