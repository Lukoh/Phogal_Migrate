package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class CoverPhoto(
    val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val width: Int,
    val height: Int,
    val color: String?,
    @SerialName("blur_hash") val blurHash: String?,
    val description: String?,
    val urls: Urls,
    val links: Links,
    val user: User
) : Parcelable