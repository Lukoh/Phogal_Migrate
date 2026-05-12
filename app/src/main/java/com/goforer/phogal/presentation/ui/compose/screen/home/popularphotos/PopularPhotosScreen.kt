package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.base.designsystem.component.CardSnackBar
import com.goforer.base.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.base.designsystem.component.ScaffoldContent
import com.goforer.phogal.R
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel.Companion.POPULAR
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosSectionUiState
import com.goforer.phogal.presentation.ui.theme.ColorBgSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularPhotosScreen(
    modifier: Modifier = Modifier,
    popularPhotosViewModel: PopularPhotosViewModel = hiltViewModel(),
    contentUiState: PopularPhotosContentUiState = rememberPopularPhotosContentUiState(popularPhotosViewModel),
    onItemClicked: (id: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {
        //To Do:: Implement the code what you want to do....
    },
    onStop: () -> Unit = {
        //To Do:: Implement the code what you want to do....
    }
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val snackbarHostState = remember { SnackbarHostState() }
    val backHandlingEnabled by remember { mutableStateOf(true) }

    BackHandler(backHandlingEnabled) {
        (contentUiState.baseUiState.context as Activity).finish()
    }

    DisposableEffect(contentUiState.baseUiState.lifecycle) {
        // Create an observer that triggers our remembered callbacks
        // for doing anything
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                currentOnStart()
            } else if (event == Lifecycle.Event.ON_STOP) {
                currentOnStop()
            }
        }

        // Add the observer to the lifecycle
        contentUiState.baseUiState.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            contentUiState.baseUiState.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        contentColor = ColorBgSecondary,
        snackbarHost = { SnackbarHost(
            snackbarHostState, snackbar = { snackbarData: SnackbarData ->
                CardSnackBar(modifier = Modifier, snackbarData)
            }
        )
        }, topBar = {
            CustomCenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.bottom_navigation_popular_photos),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {},
                actions = {}
            )
        }, content = { paddingValues ->
            ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                popularPhotosViewModel.updateOrderBy(POPULAR)
                PopularPhotosContent(
                    modifier = modifier,
                    paddingValues= paddingValues,
                    photos = contentUiState.popularPhotosUiState.photos,
                    onItemClicked = onItemClicked,
                    onViewPhotos = onViewPhotos,
                    onShowSnackBar = {
                        contentUiState.baseUiState.scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    },
                    onOpenWebView = onOpenWebView,
                    onSuccess = {
                        contentUiState.setVisibleActions(it)
                    },
                    onLoadedPhotos = {
                        contentUiState.setLoadedPhotos(it)
                    }
                )
            }
        }
    )
}