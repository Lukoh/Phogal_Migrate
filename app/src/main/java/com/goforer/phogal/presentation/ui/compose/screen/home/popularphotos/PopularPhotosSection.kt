@file:Suppress("UNCHECKED_CAST")

package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.goforer.base.designsystem.component.state.rememberLazyListState
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.LoadingPhotos
import com.goforer.phogal.presentation.ui.compose.screen.home.common.error.ErrorContent
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray7
import timber.log.Timber

/** Matches `PopularPhotosViewModel.PAGE_SIZE`. */
private const val PAGE_SIZE_HINT = 10

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
    // After recreation, LazyPagingItems first return 0 items, then the cached items.
    // This behavior/issue is resetting the LazyListState scroll position.
    // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
    // If this bug will got fixed... then have to be unblocked below code
    /*
    val visibleUpButtonState by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

     */

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
            photos.loadState.apply {
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
                        if (photos.itemCount == 0 ) {
                            sectionUiState.setUpButtonVisibilityChanged(false)
                            onSuccess(false)
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
                            onSuccess(true)
                            items(count = photos.itemCount,
                                key = photos.itemKey(
                                    key = { photo -> photo.id }
                                ),
                                contentType = photos.itemContentType()
                            ) { index ->
                                val state  = rememberPhotoItemUiState()
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
                                    state = rememberPhotoItemUiState(
                                        index = rememberSaveable { mutableIntStateOf(index) },
                                        photo = rememberSaveable { mutableStateOf(photos[index]!!) },
                                        visibleViewButton = rememberSaveable { mutableStateOf(true) },
                                        bookmarked = rememberSaveable { mutableStateOf(bookmarkViewModel.isPhotoBookmarked(photos[index]!!.id)) }
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
                    refresh is LoadState.Error -> {
                        onSuccess(false)
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
                                        photos.retry()
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
                        onSuccess(false)
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
                                        photos.retry()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!lazyListState.isScrollInProgress) {
            ShowUpButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = sectionUiState.visibleUpButton,
                onClick = {
                    sectionUiState.setUpButtonClicked()
                }
            )
        }

        LaunchedEffect(lazyListState, true, sectionUiState.clicked) {
            if (sectionUiState.clicked) {
                lazyListState.animateScrollToItem (0)
                sectionUiState.setUpButtonVisibilityChanged(false)
            }

            sectionUiState.setScrollConsumed()
        }
    }
}

private fun visibleUpButton(index: Int): Boolean {
    return when {
        index > 4 -> true
        index < 4-> false
        else -> true
    }
}