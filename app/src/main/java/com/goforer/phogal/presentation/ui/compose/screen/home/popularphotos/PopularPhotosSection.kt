@file:Suppress("UNCHECKED_CAST")

package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.goforer.base.designsystem.component.state.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.LoadingPhotos
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.phogal.presentation.ui.compose.screen.home.common.EmptyState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.ErrorRow
import timber.log.Timber

private const val PAGE_SIZE_HINT = 10
private const val UP_BUTTON_THRESHOLD = 4
private const val SCROLL_OFFSET_SIGNAL = 35

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PopularPhotosSection(
    modifier: Modifier = Modifier,
    photos: LazyPagingItems<Photo>,
    sectionUiState: PopularPhotosSectionUiState = rememberPopularPhotosSectionUiState(),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit,
    onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
) {
    val lazyListState = photos.rememberLazyListState()
    val isRefreshing = photos.loadState.refresh is LoadState.Loading

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold by remember(lazyListState) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD ||
                    lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

    // Material 3 PullToRefreshBox — default indicator is rendered automatically.
    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(0.2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = {
            photos.refresh()
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            state = lazyListState,
        ) {
            val loadState = photos.loadState

            when(loadState.refresh) {
                is LoadState.Loading -> {
                    item {
                        LoadingPhotos(
                            modifier = Modifier.padding(4.dp, 4.dp),
                            count = 3,
                            enableLoadIndicator = true
                        )
                    }

                    sectionUiState.setLoadingDone()
                }
                is LoadState.NotLoading -> {
                    if (sectionUiState.loadingDone) {
                        if (photos.itemCount == 0 ) {
                            item { EmptyState() }
                        } else {
                            items(count = photos.itemCount,
                                key = photos.itemKey(
                                    key = { photo -> photo.id }
                                ),
                                contentType = photos.itemContentType()
                            ) { index ->
                                val photo = photos[index] ?: return@items
                                val state  = rememberPhotoItemUiState(
                                    index = rememberSaveable { mutableIntStateOf(index) },
                                    photo = rememberSaveable { mutableStateOf(photo) },
                                    visibleViewButton = rememberSaveable { mutableStateOf(true) },
                                    bookmarked = rememberSaveable { mutableStateOf(bookmarkViewModel.isPhotoBookmarked(photo.id)) }
                                )
                                val padding = if (index == 0)
                                    2.dp
                                else
                                    0.5.dp
                                // After recreation, LazyPagingItems first return 0 items, then the cached items.
                                // This behavior/issue is resetting the LazyListState scroll position.
                                // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
                                // If this bug will got fixed... then have to be removed below code
                                sectionUiState.setUpButtonVisibilityChanged(visibleUpButton(index))
                                state.setIndex(index)
                                state.setPhoto(photos[index]!!)
                                state.setVisibleViewButton(true)
                                state.setBookmark(bookmarkViewModel.isPhotoBookmarked(photos[index]!!.id))

                                if (index == photos.itemCount - 1)
                                    onLoadedPhotos(true)

                                PhotoItem(
                                    modifier = modifier
                                        .padding(top = padding)
                                        .animateItem(tween(durationMillis = 250)),
                                    state = state,
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

        if (sectionUiState.loadingDone) {
            SideEffect {
                val hasItems = photos.itemCount > 0
                sectionUiState.setUpButtonVisibilityChanged(hasItems)
                onSuccess(hasItems)
            }
        }

        if (!lazyListState.isScrollInProgress) {
            ShowUpButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = isScrolledPastThreshold && sectionUiState.visibleUpButton,
                onClick = { sectionUiState.setUpButtonClicked() }
            )
        }
    }

    LaunchedEffect(lazyListState, true, sectionUiState.clicked) {
        if (sectionUiState.clicked) {
            lazyListState.animateScrollToItem (0)
            sectionUiState.setUpButtonVisibilityChanged(false)
        }

        sectionUiState.setScrollConsumed()
    }
}

private fun visibleUpButton(index: Int): Boolean {
    return when {
        index > 4 -> true
        index < 4-> false
        else -> true
    }
}