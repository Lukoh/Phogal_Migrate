package com.goforer.phogal.presentation.stateholder.business.home.common.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.common.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _followingUsers = MutableStateFlow<List<User>>(emptyList())
    val followingUsers: StateFlow<List<User>> = _followingUsers.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _followingUsers.value = withContext(ioDispatcher) {
                localDataSource.getFollowingUsers().orEmpty().toList()
            }
        }
    }

    fun setUserFollow(user: User) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                localDataSource.setFollowingUser(user)
            }
            refresh()
        }
    }

    fun isUserFollowed(user: User): Boolean = localDataSource.isUserFollowed(user)
}
