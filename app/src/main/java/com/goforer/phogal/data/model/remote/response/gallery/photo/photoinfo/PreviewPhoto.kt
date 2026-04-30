package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.Urls
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class PreviewPhoto(
    @SerialName("blur_hash") val blurHash: String,
    @SerialName("created_at") val createdAt: String,
    val id: String,
    val slug: String,
    @SerialName("updated_at") val updatedAt: String,
    val urls: Urls
) : Parcelable