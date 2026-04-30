package com.goforer.phogal.data.model.remote.response.gallery.photo.like

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class LikeResponse(
    val photo: Photo,
    val user: User
) : Parcelable