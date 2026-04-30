package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.CurrentUserCollection
import com.goforer.phogal.data.model.remote.response.gallery.common.Links
import com.goforer.phogal.data.model.remote.response.gallery.common.Tag
import com.goforer.phogal.data.model.remote.response.gallery.common.Urls
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Picture(
    val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val width: Int,
    val height: Int,
    val color: String?,
    @SerialName("blur_hash") val blurHash: String?,
    val downloads: Int,
    @SerialName("public_domain") val publicDomain: Boolean,
    val description: String?,
    val exif: Exif?,
    val location: Location?,
    val tags: List<Tag>?,
    @SerialName("current_user_collections") val currentUserCollections: List<CurrentUserCollection>?,
    val urls: Urls,
    val links: Links,
    val user: User,
    @SerialName("liked_by_user") val likedByUser: Boolean,
    var bookmarked: Boolean
) : Parcelable