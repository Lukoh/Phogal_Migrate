package com.goforer.phogal.presentation.stateholder.uistate.home.follwing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.User
import com.goforer.phogal.presentation.stateholder.business.home.common.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class FollowingUserContentUiState(
    val baseUiState: BaseUiState,
    val followingUserUiState: FollowingUserUiState,
)

@Stable
class FollowingUserUiState(
    val users: LazyPagingItems<User>,
)

@Composable
fun rememberFollowingUserContentUiState(
    followViewModel: FollowViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
): FollowingUserContentUiState {
    val followingUserUiState = rememberFollowingUserUiState(followViewModel)

    return remember(baseUiState, followViewModel) {
        FollowingUserContentUiState(
            baseUiState = baseUiState,
            followingUserUiState = followingUserUiState,
        )
    }
}

@Composable
fun rememberFollowingUserUiState(
    followViewModel: FollowViewModel
): FollowingUserUiState {
    val users = followViewModel.followedUsers.collectAsLazyPagingItems()

    // 2. 클래스에 담아서 반환
    return remember(users) {
        FollowingUserUiState(
            users = users,
        )
    }
}


