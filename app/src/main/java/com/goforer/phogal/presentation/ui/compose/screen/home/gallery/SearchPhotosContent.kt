package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.base.designsystem.animation.GenericCubicAnimationShape
import com.goforer.base.designsystem.component.Chips
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosContentState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchSectionState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberPermissionState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosContentState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosSectionState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchSectionState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen
import com.goforer.phogal.presentation.ui.theme.Black
import com.goforer.phogal.presentation.ui.theme.Blue70
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray7
import com.goforer.phogal.presentation.ui.theme.PhogalTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Gallery search screen body.
 *
 * This file is split into small composables so that recomposition stays scoped:
 *  - [SearchPhotosContent]      — container + wiring
 *  - [RecentWordsChips]         — animated chips row of recent searches
 *  - [PhotosOrInitScreen]       — switch between paging list and empty "type to search" state
 *  - [PermissionHandler]        — permission flow + rationale bottom sheet
 */
@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun SearchPhotosContent(
    modifier: Modifier = Modifier,
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    photosContentState: SearchPhotosContentState = rememberSearchPhotosContentState(
        baseUiState = rememberBaseUiState()
    ),
    searchState: SearchSectionState = rememberSearchSectionState(
        enabledState = photosContentState.enabledState
    ),
    onItemClicked: (id: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit
) {
    val photos = galleryViewModel.photos.collectAsLazyPagingItems()
    val currentQuery by galleryViewModel.query.collectAsStateWithLifecycle()
    val recentWords by galleryViewModel.recentWords.collectAsStateWithLifecycle()

    // Stable lambdas — created once per VM instance so child composables
    // don't see a new reference on every recomposition.
    val onSearch: (String) -> Unit = remember(galleryViewModel, currentQuery, photosContentState) {
        { keyword ->
            if (keyword.isNotEmpty() && keyword != currentQuery) {
                photosContentState.baseUiState.keyboardController?.hide()
                galleryViewModel.onQueryChanged(keyword)
                galleryViewModel.commitSearch()
                photosContentState.triggeredState.value = true
            }
        }
    }

    val onChipClicked: (String) -> Unit = remember(galleryViewModel, searchState, photosContentState) {
        { keyword ->
            searchState.editableInputState.textState = keyword
            photosContentState.baseUiState.keyboardController?.hide()
            galleryViewModel.onQueryChanged(keyword)
            galleryViewModel.commitSearch()
        }
    }

    Column(
        modifier = modifier.clickable {
            photosContentState.baseUiState.keyboardController?.hide()
        }
    ) {
        SearchSection(
            modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 0.dp),
            state = searchState,
            onSearched = onSearch
        )

        RecentWordsChips(
            recentWords = recentWords,
            isScrolling = photosContentState.scrollingState.value,
            triggered = photosContentState.triggeredState.value,
            onTriggeredConsumed = { photosContentState.triggeredState.value = false },
            onChipClicked = onChipClicked
        )

        PhotosOrInitScreen(
            query = currentQuery,
            photos = photos,
            onItemClicked = { photo, _ -> onItemClicked(photo.id) },
            onViewPhotos = onViewPhotos,
            onShowSnackBar = onShowSnackBar,
            onLoadSuccess = onSuccess,
            onScroll = { photosContentState.scrollingState.value = it },
            onOpenWebView = onOpenWebView
        )
    }

    PermissionHandler(
        permissions = photosContentState.permissions,
        photosContentState = photosContentState
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Animated row of recent search keywords. Hidden while scrolling. When a
 * `triggered` signal arrives, only the most recent keyword is shown (UX requirement
 * so the newly-committed keyword is highlighted without the full history noise).
 */
@Composable
private fun ColumnScope.RecentWordsChips(
    recentWords: List<String>,
    isScrolling: Boolean,
    triggered: Boolean,
    onTriggeredConsumed: () -> Unit,
    onChipClicked: (String) -> Unit
) {
    GenericCubicAnimationShape(
        visible = !isScrolling,
        duration = 250
    ) { animatedShape, visible ->
        if (!visible || recentWords.isEmpty()) return@GenericCubicAnimationShape

        val items = if (triggered) listOf(recentWords.first()) else recentWords

        Chips(
            modifier = Modifier
                .padding(top = 2.dp)
                .graphicsLayer {
                    clip = true
                    shape = animatedShape
                },
            items = items,
            textColor = Black,
            leadingIconTint = Blue70,
            onClicked = onChipClicked
        )

        // triggered is transient — reset it once the single-chip state is painted.
        if (triggered) onTriggeredConsumed()
    }
}

/**
 * Renders the paginated photo list when a query is active, or the "tap to search"
 * hint when the query is blank.
 */
@Composable
private fun ColumnScope.PhotosOrInitScreen(
    query: String,
    photos: LazyPagingItems<Photo>,
    onItemClicked: (Photo, Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onLoadSuccess: (Boolean) -> Unit,
    onScroll: (Boolean) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit
) {
    if (query.isNotBlank()) {
        SearchPhotosSection(
            modifier = Modifier
                .padding(top = 0.5.dp)
                .weight(1f),
            photos = photos,
            state = rememberSearchPhotosSectionState(),
            onItemClicked = onItemClicked,
            onViewPhotos = onViewPhotos,
            onShowSnackBar = onShowSnackBar,
            onLoadSuccess = onLoadSuccess,
            onScroll = onScroll,
            onOpenWebView = onOpenWebView
        )
    } else {
        InitScreen(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.search_photos)
        )
    }
}

/**
 * Permission flow for the gallery screen.
 * Separated out so permission UX can evolve independently of the main layout.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PermissionHandler(
    permissions: List<String>,
    photosContentState: SearchPhotosContentState
) {
    val multiplePermissionsState: MultiplePermissionsState =
        rememberMultiplePermissionsState(permissions)

    with(photosContentState) {
        CheckPermission(
            multiplePermissionsState = multiplePermissionsState,
            onPermissionGranted = {
                enabledState.value = true
                permissionState.value = false
            },
            onPermissionNotGranted = { rationale ->
                rationaleTextState.value = rationale
                enabledState.value = false
                permissionState.value = true
            }
        )

        if (permissionState.value) {
            PermissionBottomSheet(
                state = rememberPermissionState(rationaleTextState = rationaleTextState),
                onDismissedRequest = {
                    enabledState.value = false
                    permissionState.value = false
                },
                onClicked = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                    permissionState.value = false
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun PhotosContentPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        BoxWithConstraints(modifier = modifier) {
            // 1. 화면 너비에 따른 상태 정의 (Scope 참조로 경고 해결)
            val isWideScreen = maxWidth > 600.dp
            val dynamicHorizontalPadding = if (isWideScreen) 16.dp else 8.dp
            val dynamicTextStyle = if (isWideScreen) {
                typography.headlineSmall // 넓은 화면에서는 더 큰 폰트
            } else {
                typography.titleMedium
            }

            Column(
                modifier = Modifier.fillMaxSize(), // 부모 Box의 modifier 대신 fillMaxSize 권장
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchSection(
                    modifier = Modifier.padding(horizontal = dynamicHorizontalPadding),
                    onSearched = { }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.search_photos),
                        style = dynamicTextStyle.copy(
                            color = ColorSystemGray7,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
