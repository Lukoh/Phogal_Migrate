package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.goforer.base.designsystem.component.state.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.FollowingUserSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.rememberFollowingUserSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.rememberFollowingUserItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.EmptyState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.ErrorRow
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.LoadingPhotos
import timber.log.Timber

private const val UP_BUTTON_THRESHOLD = 4
private const val SCROLL_OFFSET_SIGNAL = 35

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowingUsersSection(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    sectionUiState: FollowingUserSectionUiState = rememberFollowingUserSectionUiState(),
    users: LazyPagingItems<User>,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String?) -> Unit,
    onFollow: (userUiState: User) -> Unit
) {
    val lazyListState = users.rememberLazyListState()
    val isRefreshing = users.loadState.refresh is LoadState.Loading

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold by remember(lazyListState) {
        derivedStateOf {
            !lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD &&
                    lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = users::refresh
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(0.2.dp))
                .padding(
                    0.dp,
                    contentPadding.calculateTopPadding(),
                    0.dp,
                    0.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 0.1.dp),
                state = lazyListState,
            ) {
                renderLoadState(
                    users = users,
                    onViewPhotos = onViewPhotos,
                    onOpenWebView = onOpenWebView,
                    onFollow = onFollow
                )
            }
        }

        ShowUpButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            visible = isScrolledPastThreshold,
            onClick = { sectionUiState.setClicked(true) }
        )
    }

    LaunchedEffect(lazyListState, sectionUiState.clicked) {
        if (sectionUiState.clicked) {
            lazyListState.animateScrollToItem (0)
            sectionUiState.setVisibleUpButton(false)
        }

        sectionUiState.setClicked(false)
    }
}

/**
 * Dispatches the current [LoadState] of [users] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    users: LazyPagingItems<User>,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String?) -> Unit,
    onFollow: (userUiState: User) -> Unit
) {
    val loadState = users.loadState

    when(loadState.refresh) {
        is LoadState.Loading -> {
            item {}
        }

        is LoadState.NotLoading -> {
            if (users.itemCount == 0) {
                item { EmptyState() }
            } else {
                items(
                    count = users.itemCount,
                    key = users.itemKey(
                        key = { photo -> photo.id }
                    ),
                    contentType = users.itemContentType()
                ) { index ->
                    FollowingUsersItem(
                        modifier = Modifier.animateItem(
                            tween(durationMillis = 250)
                        ),
                        followingUserItemUiState = rememberFollowingUserItemUiState(
                            index = rememberSaveable { mutableIntStateOf(index) },
                            user = rememberSaveable { mutableStateOf(users[index]!!.toString()) },
                            visibleViewButton = rememberSaveable { mutableStateOf(true) },
                            followed = rememberSaveable { mutableStateOf(true) }
                        ),
                        onViewPhotos = onViewPhotos,
                        onOpenWebView = onOpenWebView,
                        onFollow = onFollow
                    )

                    if (index == users.itemCount - 1)
                        Spacer(modifier = Modifier.height(26.dp))
                }
            }
        }

        is LoadState.Error -> {
            val error = (loadState.refresh as LoadState.Error).error
            item { ErrorRow(throwable = error, onRetry = { users.retry() }) }
        }
    }

    // Append (next-page) state is rendered independently from refresh state.
    when (loadState.append) {
        is LoadState.Loading -> {
            Timber.d("Pagination Loading")
        }
        is LoadState.Error -> {
            Timber.d("Pagination broken Error")
            val error = (loadState.append as LoadState.Error).error
            item { ErrorRow(throwable = error, onRetry = { users.retry() }) }
        }
        else -> Unit
    }
}