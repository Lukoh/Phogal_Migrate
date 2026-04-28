package com.goforer.phogal.di.module

import com.goforer.phogal.data.repository.bookmark.BookmarkRepository
import com.goforer.phogal.data.repository.bookmark.BookmarkRepositoryImpl
import com.goforer.phogal.data.repository.common.photo.info.PictureRepository
import com.goforer.phogal.data.repository.common.photo.info.PictureRepositoryImpl
import com.goforer.phogal.data.repository.common.photo.like.PictureLikeRepository
import com.goforer.phogal.data.repository.common.photo.like.PictureLikeRepositoryImpl
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepository
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepositoryImpl
import com.goforer.phogal.data.repository.follow.FollowUserRepository
import com.goforer.phogal.data.repository.follow.FollowUserRepositoryImpl
import com.goforer.phogal.data.repository.gallery.PhotosRepository
import com.goforer.phogal.data.repository.gallery.PhotosRepositoryImpl
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepository
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their concrete implementations.
 *
 * Each `*RepositoryImpl` is annotated with `@Singleton` and has an `@Inject` constructor,
 * so Hilt can construct it on demand. The `@Binds` here simply tells Hilt:
 * "when someone asks for the interface, hand them the impl."
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotosRepository(impl: PhotosRepositoryImpl): PhotosRepository

    @Binds
    @Singleton
    abstract fun bindPictureRepository(impl: PictureRepositoryImpl): PictureRepository

    @Binds
    @Singleton
    abstract fun bindPictureLikeRepository(impl: PictureLikeRepositoryImpl): PictureLikeRepository

    @Binds
    @Singleton
    abstract fun bindUserPhotosRepository(impl: UserPhotosRepositoryImpl): UserPhotosRepository

    @Binds
    @Singleton
    abstract fun bindPopularPhotosRepository(impl: PopularPhotosRepositoryImpl): PopularPhotosRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindFollowUserRepository(impl: FollowUserRepositoryImpl): FollowUserRepository
}
