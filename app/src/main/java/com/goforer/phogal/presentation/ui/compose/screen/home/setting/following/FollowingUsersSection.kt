package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.goforer.base.designsystem.component.state.rememberLazyListState
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.common.User
import com.goforer.phogal.presentation.stateholder.uistate.home.follwing.FollowingUserSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.follwing.rememberFollowingUserSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.rememberFollowingUserItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.error.ErrorContent
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.LoadingPhotos
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray7
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowingUsersSection(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    sectionUiState: FollowingUserSectionUiState = rememberFollowingUserSectionUiState(),
    users: LazyPagingItems<User>,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String?) -> Unit,
    onFollow: (user: User) -> Unit
) {
    val lazyListState = users.rememberLazyListState()
    val isRefreshing = users.loadState.refresh is LoadState.Loading

    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(0.2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = {
            users.refresh()
        }
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
                users.loadState.apply {
                    when {
                        refresh is LoadState.Loading -> {
                            item {
                                LoadingPhotos(
                                    modifier = Modifier.padding(4.dp, 4.dp),
                                    count = 3,
                                    enableLoadIndicator = true
                                )
                            }
                        }
                        refresh is LoadState.NotLoading -> {
                            if (users.itemCount == 0 ) {
                                item {
                                    Spacer(modifier = Modifier.height(320.dp))
                                    Text(
                                        text = stringResource(id = R.string.no_picture),
                                        style = MaterialTheme.typography.titleMedium.copy(color = ColorSystemGray7),
                                        modifier = Modifier.align(Alignment.Center),
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                items(count = users.itemCount,
                                    key = users.itemKey(
                                        key = { photo -> photo.id }
                                    ),
                                    contentType = users.itemContentType()
                                ) { index ->
                                    FollowingUsersItem(
                                        modifier = modifier.animateItem(
                                            tween(durationMillis = 250)
                                        ),
                                        followingUserItemUiState = rememberFollowingUserItemUiState(
                                            indexState = rememberSaveable { mutableIntStateOf(index) },
                                            userState = rememberSaveable { mutableStateOf(users[index]!!) },
                                            visibleViewButtonState = rememberSaveable { mutableStateOf(true) },
                                            followedState = rememberSaveable { mutableStateOf(true) }
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
                        refresh is LoadState.Error -> {
                            item {
                                val throwable = (refresh as LoadState.Error).error

                                AnimatedVisibility(
                                    visible = true,
                                    modifier = Modifier,
                                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                                            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                                            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                                ) {
                                    ErrorContent(
                                        modifier = modifier,
                                        title = stringResource(id = R.string.error_dialog_title),
                                        message = throwable.message
                                            ?: stringResource(id = R.string.error_dialog_content),
                                        onRetry = {
                                            users.retry()
                                        }
                                    )
                                }
                            }
                        }
                        append is LoadState.Loading -> {
                            Timber.d("Pagination Loading")
                        }
                        append is LoadState.Error -> {
                            Timber.d("Pagination broken Error")
                            item {
                                val throwable = (append as LoadState.Error).error

                                AnimatedVisibility(
                                    visible = true,
                                    modifier = Modifier,
                                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                                            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                                            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                                ) {
                                    ErrorContent(
                                        title = stringResource(id = R.string.error_dialog_title),
                                        message = throwable.message
                                            ?: stringResource(id = R.string.error_dialog_content),
                                        onRetry = {
                                            users.retry()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // PullToRefreshBox renders its own default Indicator — no separate one needed.
        if (!lazyListState.isScrollInProgress) {
            ShowUpButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = sectionUiState.visibleUpButtonState.value,
                onClick = {
                    sectionUiState.clickedState.value = true
                }
            )
        }

        LaunchedEffect(lazyListState, true, sectionUiState.clickedState.value) {
            if (sectionUiState.clickedState.value) {
                lazyListState.animateScrollToItem (0)
                sectionUiState.visibleUpButtonState.value = false
            }

            sectionUiState.clickedState.value = false
        }
    }
}