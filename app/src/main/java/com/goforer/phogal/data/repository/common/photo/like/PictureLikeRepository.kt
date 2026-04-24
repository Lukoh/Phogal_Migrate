package com.goforer.phogal.data.repository.common.photo.like

import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.like.LikeResponse

/**
 * Repository for toggling the "like" status on a picture.
 *
 * Merges what used to be `PostPictureLikeRepository` + `DeletePictureLikeRepository`
 * into a single cohesive contract. A ViewModel that cares about like state should
 * depend on this one interface instead of two.
 */
interface PictureLikeRepository {

    /** Mark the picture as liked by the current user. */
    suspend fun like(pictureId: String): NetworkResult<LikeResponse>

    /** Undo a like on the picture. */
    suspend fun unlike(pictureId: String): NetworkResult<LikeResponse>
}
