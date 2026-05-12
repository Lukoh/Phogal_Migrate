package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import android.os.Parcel
import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class Position(
    val latitude: Double?,
    val longitude: Double?
) : Parcelable {
    companion object {
        fun empty() = Position(
            latitude = 0.0,
            longitude = 0.0
        )
    }
}