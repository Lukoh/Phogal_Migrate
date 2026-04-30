package com.goforer.phogal.data.repository.follow

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.repository.follow.paging.FollowUserPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUserRepositoryImpl @Inject constructor() : FollowUserRepository {
    override fun followedUsers(followedUsers: List<User>, pageSize: Int): Flow<PagingData<User>> {
        val followUsersPagingFlow = Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { FollowUserPagingSource(followedUsers) }
        ).flow

        return followUsersPagingFlow
    }
}