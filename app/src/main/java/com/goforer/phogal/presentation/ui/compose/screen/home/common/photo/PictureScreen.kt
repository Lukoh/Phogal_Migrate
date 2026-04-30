package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.base.designsystem.component.CardSnackBar
import com.goforer.base.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.base.designsystem.component.ScaffoldContent
import com.goforer.base.designsystem.component.dialog.ErrorDialog
import com.goforer.phogal.R
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoContentUiState
import com.goforer.phogal.presentation.ui.theme.Red60
import kotlinx.coroutines.launch

/**
 * Single-picture screen.
 *
 * Compared to the legacy version, all three ViewModels (`Picture`, `PictureLike`,
 * `PictureUnlike`) have been collapsed into one `PictureViewModel`. The top bar's
 * like/bookmark icons read state directly from `pictureUiState`, and the
 * error dialog for a failed like/unlike is driven by a single `likeActionState`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureScreen(
    modifier: Modifier = Modifier,
    pictureViewModel: PictureViewModel,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    state: PhotoContentUiState = rememberPhotoContentUiState(),
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onBackPressed: () -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val snackbarHostState = remember { SnackbarHostState() }
    val backHandlingEnabled by remember { mutableStateOf(true) }

    BackHandler(backHandlingEnabled) { onBackPressed() }

    DisposableEffect(state.baseUiState.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart()
                Lifecycle.Event.ON_STOP  -> currentOnStop()
                else -> Unit
            }
        }
        state.baseUiState.lifecycle.addObserver(observer)
        onDispose { state.baseUiState.lifecycle.removeObserver(observer) }
    }

    // Kick off the load whenever the id changes (canonical replacement for the
    // legacy `enabledLoadState` one-shot gate).
    LaunchedEffect(state.idState.value) {
        pictureViewModel.loadPicture(state.idState.value)
    }

    // Top-bar icons read from the authoritative pictureUiState.
    val pictureUiState by pictureViewModel.pictureUiState.collectAsStateWithLifecycle()
    val currentPicture = (pictureUiState as? UiState.Success)?.data
    val isLikedByUser = currentPicture?.likedByUser == true

    // Observe like/unlike transient result so we can surface an error dialog.
    LikeActionHandle(pictureViewModel = pictureViewModel)

    Scaffold(
        contentColor = Color.White,
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                snackbar = { snackbarData: SnackbarData ->
                    CardSnackBar(modifier = Modifier, snackbarData)
                }
            )
        },
        topBar = {
            CustomCenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.picture_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Picture"
                        )
                    }
                },
                actions = {
                    if (state.visibleActionsState.value && currentPicture != null) {
                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (isLikedByUser) Red60 else Color.Black
                            ),
                            onClick = { pictureViewModel.toggleLike() }
                        ) {
                            Icon(
                                imageVector = if (isLikedByUser) {
                                    ImageVector.vectorResource(id = R.drawable.ic_like_on)
                                } else {
                                    ImageVector.vectorResource(id = R.drawable.ic_like_off)
                                },
                                contentDescription = "Like"
                            )
                        }

                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (state.enabledBookmarkState.value) {
                                    Red60
                                } else {
                                    Color.Black
                                }
                            ),
                            onClick = {
                                bookmarkViewModel.setBookmarkPicture(currentPicture)
                                state.enabledBookmarkState.value = !state.enabledBookmarkState.value
                            }
                        ) {
                            Icon(
                                imageVector = if (state.enabledBookmarkState.value) {
                                    ImageVector.vectorResource(id = R.drawable.ic_bookmark_on)
                                } else {
                                    ImageVector.vectorResource(id = R.drawable.ic_bookmark_off)
                                },
                                contentDescription = "Bookmark"
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            ScaffoldContent(topInterval = 0.dp) {
                PictureContent(
                    modifier = modifier,
                    contentPadding = paddingValues,
                    pictureViewModel = pictureViewModel,
                    state = state,
                    onViewPhotos = onViewPhotos,
                    onShowSnackBar = {
                        state.baseUiState.scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    },
                    onShownPhoto = { picture ->
                        state.visibleActionsState.value = true
                        state.enabledBookmarkState.value = bookmarkViewModel.isPhotoBookmarked(picture)
                    },
                    onOpenWebView = onOpenWebView,
                    onSuccess = { isSuccessful ->
                        if (!isSuccessful) state.visibleActionsState.value = false
                    }
                )
            }
        }
    )
}

/**
 * Observes the transient [com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel.likeActionState]
 * and shows an error dialog on failure. Replaces the legacy `LikeResponseHandle` +
 * `UnlikeResponseHandle` pair — one handler is enough because the VM now exposes a
 * single state for both the POST and the DELETE path.
 */
@Composable
private fun LikeActionHandle(pictureViewModel: PictureViewModel) {
    val likeActionState by pictureViewModel.likeActionState.collectAsStateWithLifecycle()
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(likeActionState) {
        if (likeActionState is UiState.Error) showErrorDialog.value = true
    }

    val errorState = likeActionState as? UiState.Error ?: return
    if (!showErrorDialog.value) return

    AnimatedVisibility(
        visible = true,
        modifier = Modifier,
        enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
        exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
    ) {
        ErrorDialog(
            title = if (errorState.code !in 200..299) {
                stringResource(id = R.string.error_dialog_network_title)
            } else {
                stringResource(id = R.string.error_dialog_title)
            },
            text = errorState.message
        ) {
            showErrorDialog.value = false
            pictureViewModel.consumeLikeAction()
        }
    }
}
