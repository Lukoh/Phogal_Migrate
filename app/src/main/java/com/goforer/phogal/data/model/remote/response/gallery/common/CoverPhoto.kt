package com.goforer.phogal.data.model.remote.response.gallery.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Immutable // Compose 리컴포지션 최적화를 위해 추가
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
) : Parcelable {
    companion object {
        fun empty() = CoverPhoto(
            id = "",
            createdAt = "",
            updatedAt = "",
            width = 0,
            height = 0,
            color = null,
            blurHash = null,
            description = null,
            urls = Urls.empty(),
            links = Links.empty(),
            user = User.empty()
        )
    }
}