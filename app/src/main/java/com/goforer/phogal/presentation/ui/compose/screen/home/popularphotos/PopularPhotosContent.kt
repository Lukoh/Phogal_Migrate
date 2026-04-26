package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosSectionUiState
import com.goforer.phogal.presentation.ui.theme.PhogalTheme

/**
 * Popular-photos feed body. The VM's `photos: StateFlow<PagingData<Photo>>` is
 * started eagerly (see `PopularPhotosViewModel`), so all this composable does is
 * collect it into `LazyPagingItems` and pass it down.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PopularPhotosContent(
    modifier: Modifier = Modifier,
    photos: LazyPagingItems<Photo>,
    onItemClicked: (id: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit,
    onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
) {
    PopularPhotosSection(
        modifier = modifier,
        photos = photos,
        state = rememberPopularPhotosSectionUiState(),
        onItemClicked = { photo, _ -> onItemClicked(photo.id) },
        onViewPhotos = onViewPhotos,
        onShowSnackBar = onShowSnackBar,
        onOpenWebView = onOpenWebView,
        onSuccess = onSuccess,
        onLoadedPhotos = onLoadedPhotos
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Suppress("unused")
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun PopularPhotosContentPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        BoxWithConstraints(modifier = modifier) {
            Column(
                modifier = modifier.padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoxWithConstraints(modifier = Modifier.weight(1f)) { }
            }
        }
    }
}
