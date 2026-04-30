package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Sponsor(
    @SerialName("accepted_tos") val acceptedTos: Boolean,
    val bio: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("for_hire") val forHire: Boolean,
    val id: String,
    @SerialName("instagram_username") val instagramUsername: String? = null,
    val last_name: String?,
    val links: Links,
    val location: String?,
    val name: String,
    val portfolio_url: String?,
    @SerialName("profile_image") val profileImage: ProfileImage,
    val social: Social,
    @SerialName("total_collections") val totalCollections: Int,
    @SerialName("total_likes") val totalLikes: Int,
    @SerialName("total_photos") val totalPhotos: Int,
    @SerialName("twitter_username") val twitterUsername: String?,
    @SerialName("updated_at") val updatedAt: String,
    val username: String
) : Parcelable