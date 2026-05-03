package com.goforer.phogal.presentation.stateholder.uistate.home.setting.bookmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class BookmarkContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val bookmarkUiState: BookmarkUiState,

    private val _enabledLoadPhotos: MutableState<Boolean>
) {
    val enabledLoadPhotos: Boolean get() = _enabledLoadPhotos.value

    fun setEnabledLoadPhotos(enabledLoadPhotos: Boolean) {
        _enabledLoadPhotos.value = enabledLoadPhotos
    }
}

@Stable
class BookmarkUiState(
    val bookmarkedPictures: LazyPagingItems<Picture>,
)

@Composable
fun rememberBookmarkContentUiState(
    bookmarkViewModel: BookmarkViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    enabledLoadPhotos: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) }
): BookmarkContentUiState {
    val bookmarkUiState = rememberBookmarkUiState(bookmarkViewModel)

    return remember(baseUiState, bookmarkViewModel, enabledLoadPhotos) {
        BookmarkContentUiState(
            baseUiState = baseUiState,
            bookmarkUiState = bookmarkUiState,
            _enabledLoadPhotos = enabledLoadPhotos
        )
    }
}

@Composable
fun rememberBookmarkUiState(
    bookmarkViewModel: BookmarkViewModel
): BookmarkUiState {
    val bookmarkedPictures = bookmarkViewModel.bookmarkedPictures.collectAsLazyPagingItems()

    return remember(bookmarkedPictures) {
        BookmarkUiState(
            bookmarkedPictures = bookmarkedPictures,
        )
    }
}