package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.CoverPhoto
import com.goforer.phogal.data.model.remote.response.gallery.common.Links
import com.goforer.phogal.data.model.remote.response.gallery.common.Tag
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.common.user.UserLinks
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Result(
    @SerialName("cover_photo") val coverPhoto: CoverPhoto?,
    val curated: Boolean,
    val description: String?,
    val featured: Boolean,
    val id: String,
    @SerialName("last_collected_at") val lastCollectedAt: String,
    val links: Links,
    @SerialName("preview_photos") val previewPhotos: List<PreviewPhoto>,
    val private: Boolean,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("share_key") val shareKey: String,
    val tags: List<Tag>,
    val title: String,
    @SerialName("total_photos") val totalPhotos: Long,
    @SerialName("updated_at")val updatedAt: String,
    val user: User
) : Parcelable