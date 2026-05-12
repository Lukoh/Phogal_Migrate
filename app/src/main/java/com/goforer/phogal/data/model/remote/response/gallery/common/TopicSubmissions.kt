package com.goforer.phogal.data.model.remote.response.gallery.common

import android.os.Parcel
import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class TopicSubmissions(
    val wallpapers: Wallpapers?
) : Parcelable {
    companion object {
        fun empty() = TopicSubmissions(
            wallpapers = null // 또는 필요에 따라 Wallpapers.empty()
        )
    }
}