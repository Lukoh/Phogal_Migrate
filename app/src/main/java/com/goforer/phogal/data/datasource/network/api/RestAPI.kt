package com.goforer.phogal.data.datasource.network.api

import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.like.LikeResponse
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.model.remote.response.gallery.photos.PhotosResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit definition of the Unsplash API surface used by Phogal.
 *
 * Each endpoint is a `suspend fun` returning `Response<T>`. Repositories wrap these calls
 * with [com.goforer.phogal.data.datasource.network.safeApiCall] to convert the raw
 * response (or any thrown exception) into a type-safe
 * [com.goforer.phogal.data.datasource.network.NetworkResult].
 */
interface RestAPI {

    @GET("search/photos")
    suspend fun getPhotos(
        @Query("client_id") clientId: String,
        @Query("query") keyword: String,
        @Query("page") page: Int?,
        @Query("per_page") perPage: Int?
    ): Response<PhotosResponse>

    @GET("photos/{id}")
    suspend fun getPhoto(
        @Path("id") id: String,
        @Query("client_id") clientId: String
    ): Response<Picture>

    @GET("users/{username}")
    suspend fun getUserPublicProfile(
        @Path("username") username: String
    ): Response<User>

    @GET("users/{username}/photos")
    suspend fun getUserPhotos(
        @Path("username") username: String,
        @Query("client_id") clientId: String,
        @Query("page") page: Int?,
        @Query("per_page") perPage: Int?
    ): Response<List<Photo>>

    @POST("photos/{id}/like")
    suspend fun postLike(
        @Path("id") id: String,
        @Query("client_id") clientId: String
    ): Response<LikeResponse>

    @DELETE("photos/{id}/like")
    suspend fun deleteLike(
        @Path("id") id: String,
        @Query("client_id") clientId: String
    ): Response<LikeResponse>

    @GET("photos")
    suspend fun getPopularPhotos(
        @Query("client_id") clientId: String,
        @Query("page") page: Int?,
        @Query("per_page") perPage: Int?,
        @Query("order_by") orderBy: String,
    ): Response<List<Photo>>
}
