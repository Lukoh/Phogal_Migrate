package com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.base.customtab.openCustomTab
import com.goforer.phogal.R
import com.goforer.phogal.presentation.stateholder.business.home.common.user.UserPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotosContentState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosContentState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosSectionState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.SearchSection
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray7
import com.goforer.phogal.presentation.ui.theme.PhogalTheme

@Composable
fun UserPhotosContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    userPhotosViewModel: UserPhotosViewModel = hiltViewModel(),
    state: UserPhotosContentState = rememberUserPhotosContentState(),
    onItemClicked: (id: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit
) {
    // Kick off the Paging stream whenever the target user changes.
    LaunchedEffect(state.nameState.value) {
        if (state.nameState.value.isNotBlank()) {
            userPhotosViewModel.loadFor(state.nameState.value)
        }
    }

    val photos = userPhotosViewModel.photos.collectAsLazyPagingItems()

    if (state.nameState.value.isNotBlank()) {
        UserPhotosSection(
            modifier = Modifier.padding(top = 0.5.dp),
            contentPadding = contentPadding,
            photos = photos,
            state = rememberUserPhotosSectionState(),
            onItemClicked = { photo, _ -> onItemClicked(photo.id) },
            onViewPhotos = { _, _, _, _ -> },
            onShowSnackBar = onShowSnackBar,
            onOpenWebView = { _, url ->
                openCustomTab(state.baseUiState.context, url)
            },
            onSuccess = onSuccess
        )
    } else {
        InitScreen(
            modifier = modifier,
            text = stringResource(id = R.string.search_photos)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun UserPhotosContentPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        BoxWithConstraints(modifier = modifier) {
            // maxWidth를 변수에 할당하여 사용 (에러 해결 핵심)
            val isWideScreen = maxWidth > 600.dp
            val dynamicPadding = if (isWideScreen) 32.dp else 2.dp
            val dynamicFontSize = if (isWideScreen) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleMedium
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchSection(
                    modifier = Modifier.padding(horizontal = dynamicPadding),
                    onSearched = { }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.search_photos),
                        style = dynamicFontSize.copy( // 크기에 따른 동적 스타일 적용
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
