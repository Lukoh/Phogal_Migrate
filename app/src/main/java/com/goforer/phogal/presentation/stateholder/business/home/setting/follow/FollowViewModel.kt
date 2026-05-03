package com.goforer.phogal.presentation.stateholder.business.home.setting.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.follow.FollowUserRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel.Companion.PAGE_SIZE
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel.Companion.STOP_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Exposes the list of locally-followed users. Pure local-storage VM; no network.
 * I/O is dispatched to [ioDispatcher] so the main thread never blocks on disk.
 */
@HiltViewModel
class FollowViewModel @Inject constructor(
    private val localDataSource: LocalDataSource,
    followUserRepository: FollowUserRepository,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _followedUsers = MutableStateFlow<List<User>>(emptyList())

    val users: StateFlow<List<User>> = localDataSource.followingUsersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val followedUsers: StateFlow<PagingData<User>> = users
        .flatMapLatest { users ->
            followUserRepository.followedUsers(users.toMutableList(), pageSize = PAGE_SIZE)
        }
        .cachedIn(viewModelScope) // 페이징 상태 유지
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _followedUsers.value = withContext(ioDispatcher) {
                localDataSource.followingUsersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()).value.toMutableList().toList()
            }
        }
    }

    fun setUserFollow(user: User) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                localDataSource.toggleFollowingUser(user)
            }
            refresh()
        }
    }

    fun isUserFollowed(user: User): Boolean = localDataSource.isUserFollowedFlow(user).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value
}
