package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.error.ErrorContent
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray7
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

private const val PAGE_SIZE_HINT = 10
private const val UP_BUTTON_THRESHOLD = 4
private const val SCROLL_OFFSET_SIGNAL = 35

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchPhotosSection(
    modifier: Modifier = Modifier,
    photos: LazyPagingItems<Photo>,
    sectionUiState: SearchPhotosSectionUiState = rememberSearchPhotosSectionUiState(rememberSaveable { mutableStateOf(true) }),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onLoadSuccess: (isSuccessful: Boolean) -> Unit,
    onScroll: (isScrolling: Boolean) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit
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

    // Propagate scroll signal to parent — only when isScrollInProgress changes,
    // not on every pixel of scrolling.
    LaunchedEffect(lazyListState) {
        snapshotFlowScrollState(lazyListState).collect { scrolling ->
            onScroll(scrolling)
        }
    }

    // Nav3-stable Material 3 PullToRefreshBox replaces the deprecated
    // androidx.compose.material.pullrefresh.* APIs. The container handles the
    // refresh indicator itself — no separate PullRefreshIndicator needed.
    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(0.2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = { photos.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            state = lazyListState
        ) {
            renderLoadState(
                photos = photos,
                sectionUiState = sectionUiState,
                bookmarkViewModel = bookmarkViewModel,
                onItemClicked = onItemClicked,
                onViewPhotos = onViewPhotos,
                onShowSnackBar = onShowSnackBar,
                onOpenWebView = onOpenWebView,
                onLoadSuccess = onLoadSuccess
            )
        }

        // PullToRefreshBox renders its own default Indicator (Material 3 spec-compliant).
        // To customize, pass `indicator = { ... }` to PullToRefreshBox above.

        // Show up-button only when user has scrolled past the threshold and isn't
        // actively scrolling (prevents the button from flickering during drags).
        if (!lazyListState.isScrollInProgress) {
            ShowUpButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = isScrolledPastThreshold && sectionUiState.visibleUpButton,
                onClick = { sectionUiState.setUpButtonClicked() }
            )
        }
    }

    // Animate scroll-to-top when the up-button was tapped.
    LaunchedEffect(sectionUiState.clicked) {
        if (sectionUiState.clicked) {
            lazyListState.animateScrollToItem(0)
            sectionUiState.setUpButtonVisibilityChanged(false)
            sectionUiState.setScrollConsumed()
        }
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
    sectionUiState: SearchPhotosSectionUiState,
    bookmarkViewModel: BookmarkViewModel,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onLoadSuccess: (Boolean) -> Unit
) {
    val loadState = photos.loadState

    when (loadState.refresh) {
        is LoadState.Loading -> {
            item { LoadingRow() }
        }

        is LoadState.NotLoading if photos.itemCount == 0 -> {
            onLoadSuccess(false)
            sectionUiState.setUpButtonVisibilityChanged(false)
            item { EmptyState() }
        }

        is LoadState.NotLoading -> {
            onLoadSuccess(true)
            photoItems(
                photos = photos,
                bookmarkViewModel = bookmarkViewModel,
                onItemClicked = onItemClicked,
                onViewPhotos = onViewPhotos,
                onShowSnackBar = onShowSnackBar,
                onOpenWebView = onOpenWebView
            )
        }

        is LoadState.Error -> {
            onLoadSuccess(false)
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
            onLoadSuccess(false)
            val error = (loadState.append as LoadState.Error).error
            item { ErrorRow(throwable = error, onRetry = { photos.retry() }) }
        }
        else -> Unit
    }
}

/**
 * Emits the actual photo items. `itemKey` + `itemContentType` are critical for
 * Paging 3 recomposition stability across config changes.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.photoItems(
    photos: LazyPagingItems<Photo>,
    bookmarkViewModel: BookmarkViewModel,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit
) {
    items(
        count = photos.itemCount,
        key = photos.itemKey { it.id },
        contentType = photos.itemContentType()
    ) { index ->
        val photo = photos[index] ?: return@items

        PhotoItem(
            modifier = Modifier.animateItem(tween(durationMillis = 250)),
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

        // Spacer only at the end of a short result list.
        if (photos.itemCount < PAGE_SIZE_HINT && index == photos.itemCount - 1) {
            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
private fun LoadingRow(modifier: Modifier = Modifier) {
    LoadingPhotos(
        modifier = modifier.padding(4.dp, 4.dp),
        count = 3,
        enableLoadIndicator = true
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(320.dp))
        Text(
            text = stringResource(id = R.string.no_picture),
            style = MaterialTheme.typography.titleMedium.copy(color = ColorSystemGray7),
            modifier = Modifier.align(Alignment.Center),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorRow(
    throwable: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        modifier = modifier,
        enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) + fadeIn() +
                expandIn(expandFrom = Alignment.TopStart),
        exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) + fadeOut() +
                shrinkOut(shrinkTowards = Alignment.TopStart)
    ) {
        ErrorContent(
            title = stringResource(id = R.string.error_dialog_title),
            message = throwable.message ?: stringResource(id = R.string.error_dialog_content),
            onRetry = onRetry
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Converts a LazyListState into a Flow<Boolean> that emits whenever the
 * `isScrollInProgress` value changes. Uses `snapshotFlow` so Compose's snapshot
 * system — not polling — drives the emissions.
 */
private fun snapshotFlowScrollState(state: LazyListState) =
    androidx.compose.runtime.snapshotFlow { state.isScrollInProgress }
        .distinctUntilChanged()
