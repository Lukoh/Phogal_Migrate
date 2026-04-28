package com.goforer.phogal.presentation.stateholder.uistate.home.bookmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.common.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class BookmarkContentUiState(
    val baseUiState: BaseUiState,
    val bookmarkUiState: BookmarkUiState,
)

@Stable
class BookmarkUiState(
    val bookmarkedPictures: MutableList<Picture>,
)

@Composable
fun rememberBookmarkContentUiState(
    bookmarkViewModel: BookmarkViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
): BookmarkContentUiState {
    val bookmarkUiState = rememberBookmarkUiState(bookmarkViewModel)

    return remember(baseUiState, bookmarkViewModel) {
        BookmarkContentUiState(
            baseUiState = baseUiState,
            bookmarkUiState = bookmarkUiState,
        )
    }
}

@Composable
fun rememberBookmarkUiState(
    bookmarkViewModel: BookmarkViewModel
): BookmarkUiState {
    val bookmarkedPictures = bookmarkViewModel.bookmarkedPictures.collectAsStateWithLifecycle()

    // 2. 클래스에 담아서 반환
    return remember(bookmarkedPictures) {
        BookmarkUiState(
            bookmarkedPictures = bookmarkedPictures.value.toMutableList(),
        )
    }
}