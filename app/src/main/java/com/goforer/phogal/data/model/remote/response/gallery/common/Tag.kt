package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class Tag(
    val source: Source?,
    val title: String?,
    val type: String?
) : Parcelable