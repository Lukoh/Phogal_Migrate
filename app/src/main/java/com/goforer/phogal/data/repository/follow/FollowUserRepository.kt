package com.goforer.phogal.data.repository.follow

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.coroutines.flow.Flow

interface FollowUserRepository {
    /**
     * @param followedUsers     Followed the users (non-blank)
     * @param pageSize  page size for [androidx.paging.PagingConfig]; also used as initial load size
     */
    fun followedUsers(followedUsers: List<User>, pageSize: Int): Flow<PagingData<User>>
}