package com.goforer.phogal.data.model.local.home.gallery

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class PictureArgument(
    val id: String,
    val visibleViewPhotosButton: Boolean
) : Parcelable
