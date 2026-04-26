package com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.common.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen

@Composable
fun BookmarkedPhotosContent(
    modifier: Modifier = Modifier,
    bookmarkedPictures: MutableList<Picture>,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    enabledLoadPhotosState: MutableState<Boolean>,
    onItemClicked: (item: Picture, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit
) {
    if (bookmarkedPictures.isNotEmpty()) {
        BookmarkedPhotosSection(
            modifier = modifier,
            contentPadding = contentPadding,
            photos = bookmarkedPictures,
            onItemClicked = onItemClicked,
            onViewPhotos = onViewPhotos,
            onOpenWebView = onOpenWebView
        )
    } else {
        if (enabledLoadPhotosState.value) {
            BoxWithConstraints(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val isTablet = maxWidth > 600.dp

                InitScreen(
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 16.dp),
                    text = stringResource(id = R.string.setting_no_bookmarked_photos)
                )
            }
        }
    }
}