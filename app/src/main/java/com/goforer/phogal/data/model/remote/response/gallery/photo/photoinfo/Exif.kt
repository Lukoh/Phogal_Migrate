package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Exif(
    val aperture: String?,
    @SerialName("exposure_time") val exposureTime: String?,
    @SerialName("focal_length") val focalLength: String?,
    val iso: Int?,
    val make: String?,
    val model: String?,
    val name: String?
) : Parcelable