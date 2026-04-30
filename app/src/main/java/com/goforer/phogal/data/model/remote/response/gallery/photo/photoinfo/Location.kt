package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class Location(
    val city: String?,
    val country: String?,
    val name: String?,
    val position: Position
) : Parcelable