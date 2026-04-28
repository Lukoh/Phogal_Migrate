package com.goforer.phogal.data.repository.follow

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.common.User
import kotlinx.coroutines.flow.Flow

/**
 * Follow users on Unsplash
 *
 * Consumers should typically call [followedUsers] from a ViewModel-scoped coroutine and
 * forward the resulting [Flow] through `cachedIn(viewModelScope)` before exposing it
 * to the UI layer as a `StateFlow<PagingData<User>>`.
 */
interface FollowUserRepository {
    /**
     * @param followedUsers     Followed the users (non-blank)
     * @param pageSize  page size for [androidx.paging.PagingConfig]; also used as initial load size
     */
    fun followedUsers(followedUsers: List<User>, pageSize: Int): Flow<PagingData<User>>
}