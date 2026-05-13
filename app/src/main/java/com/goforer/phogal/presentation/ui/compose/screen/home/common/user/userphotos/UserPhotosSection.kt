@file:Suppress("UNCHECKED_CAST")

package com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import com.goforer.base.designsystem.component.state.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.EmptyState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.ErrorRow
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import kotlinx.coroutines.launch
import timber.log.Timber

/** Matches `UserPhotosViewModel.PAGE_SIZE`. Kept local so the section stays decoupled. */
private const val PAGE_SIZE_HINT = 10
private const val UP_BUTTON_THRESHOLD = 4
private const val SCROLL_OFFSET_SIGNAL = 35

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    photos: LazyPagingItems<Photo>,
    sectionUiState: UserPhotosSectionUiState = rememberUserPhotosSectionUiState(),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit
) {
    val lazyListState = photos.rememberLazyListState()
    val isRefreshing = photos.loadState.refresh is LoadState.Loading

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold by remember(lazyListState) {
        derivedStateOf {
            !lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD &&
                    lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

    val layoutDirection = LocalLayoutDirection.current

    // Material 3 PullToRefreshBox (replaces deprecated material.pullrefresh.*).
    PullToRefreshBox(
        modifier = modifier
            .clip(RoundedCornerShape(0.2.dp))
            .padding(
                0.dp,
                paddingValues.calculateTopPadding(),
                0.dp,
                0.dp
            ),
        isRefreshing = isRefreshing,
        onRefresh = { photos.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            state = lazyListState,
            contentPadding = PaddingValues(
                start = paddingValues.calculateLeftPadding(layoutDirection),
                top = 0.dp,
                end = paddingValues.calculateRightPadding(layoutDirection) ,
                bottom = paddingValues.calculateBottomPadding() + 24.dp
            )
        ) {
            renderLoadState(
                photos = photos,
                sectionUiState = sectionUiState,
                bookmarkViewModel = bookmarkViewModel,
                onItemClicked = onItemClicked,
                onViewPhotos = onViewPhotos,
                onShowSnackBar = onShowSnackBar,
                onOpenWebView = onOpenWebView,
                onSuccess = onSuccess
            )
        }

        ShowUpButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            visible = isScrolledPastThreshold,
            onClick = {
                sectionUiState.scope.launch {
                    lazyListState.animateScrollToItem (0)
                }
            }
        )
    }
}

/**
* Dispatches the current [LoadState] of [photos] into the appropriate sub-renderer.
* Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
* directly without re-wrapping.
*/
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    photos: LazyPagingItems<Photo>,
    sectionUiState: UserPhotosSectionUiState,
    bookmarkViewModel: BookmarkViewModel,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (Boolean) -> Unit
) {
    val loadState = photos.loadState

    when(loadState.refresh) {
        is LoadState.Loading -> {
            item {}
            sectionUiState.setLoadingDone()
        }
        is LoadState.NotLoading -> {
            if (sectionUiState.loadingDone) {
                if (photos.itemCount == 0 ) {
                    onSuccess(false)
                    item { EmptyState() }
                } else {
                    onSuccess(true)
                    items(
                        count = photos.itemCount,
                        key = { index ->
                            val photo = photos.peek(index)
                            "${photo?.id ?: index}_$index"
                        },
                        contentType = photos.itemContentType()
                    ) { index ->
                        val photo = photos[index] ?: return@items

                        PhotoItem(
                            Modifier.animateItem(
                                placementSpec = tween(durationMillis = 250)
                            ),
                            state = rememberPhotoItemUiState(
                                index = rememberSaveable { mutableIntStateOf(index) },
                                photo = rememberSaveable { mutableStateOf(photo) },
                                visibleViewButton = rememberSaveable { mutableStateOf(true) },
                                bookmarked = rememberSaveable {
                                    mutableStateOf(bookmarkViewModel.isPhotoBookmarked(photo.id))
                                }
                            ),
                            onItemClicked = onItemClicked,
                            onViewPhotos = onViewPhotos,
                            onShowSnackBar = onShowSnackBar,
                            onOpenWebView = onOpenWebView
                        )
                        if (photos.itemCount < PAGE_SIZE_HINT && index == photos.itemCount - 1)
                            Spacer(modifier = Modifier.height(26.dp))
                    }
                }
            }
        }
        is LoadState.Error -> {
            onSuccess(false)
            val error = (loadState.refresh as LoadState.Error).error
            item { ErrorRow(throwable = error, onRetry = { photos.retry() }) }
        }
    }

    // Append (next-page) state is rendered independently from refresh state.
    when (loadState.append) {
        is LoadState.Loading -> {
            Timber.d("Pagination Loading")
        }

        is LoadState.Error -> {
            Timber.d("Pagination broken Error")
            onSuccess(false)
            val error = (loadState.append as LoadState.Error).error
            item { ErrorRow(throwable = error, onRetry = { photos.retry() }) }
        }

        else -> Unit
    }
}

/*
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun ListSectionPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        val lazyListState: LazyListState = rememberLazyListState()
        val photos = mutableListOf(
            Document("_SBS","news", "2017-01-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_JTBC","news", "2017-02-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_KBS","news", "2017-03-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-04-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457,"http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-05-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457,"http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-06-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457,"http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-07-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-08-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-09-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-10-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634"),
            Document("_SBS","news", "2017-11-21T15:59:30.000+09:00", "한국경제TV","http://v.media.daum.net/v/20170621155930002",457, "http://t1.daumcdn.net/news/201706/21/kedtv/20170621155930292vyyx.jpg", 185,"https://search2.kakaocdn.net/argon/138x78_80_pr/FRkbdWEKr4F", "https://search2.kakaocdn.net/argon/130x130_85_c/36hQpoTrVZp","AOA 지민·김용만, 돼지꼬리 맛에 정신혼미 ‘극찬세례’", "http://tv.kakao.com/channel/2653417/cliplink/304487728?playlistId=87634")
        )

        BoxWithConstraints(modifier = modifier) {
            LazyColumn(
                modifier = Modifier,
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(
                    photos,
                    key = { _, item -> item.datetime },
                    itemContent = { index, item ->
                        PhotoItem(
                            modifier = modifier,
                            index = index,
                            photo = item,
                            onItemClicked = { _, _ -> }
                        )
                    })
            }

            AnimatedVisibility(
                visible = true,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(bottom = 4.dp, end = 8.dp),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                    }
                ) {
                    Text("Up!")
                }
            }
        }
    }
}

 */