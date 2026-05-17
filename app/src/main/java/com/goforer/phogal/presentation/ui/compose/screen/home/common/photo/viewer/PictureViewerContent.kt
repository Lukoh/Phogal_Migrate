package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.size.Size
import com.goforer.base.designsystem.animation.GenericCubicAnimationShape
import com.goforer.base.designsystem.component.IconButton
import com.goforer.base.designsystem.component.dialog.AlertDialog
import com.goforer.base.designsystem.component.dialog.AutoDismissDialog
import com.goforer.base.designsystem.component.loadImagePainter
import com.goforer.base.utils.download.PhotoAlreadyExistsException
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Exif
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.business.home.download.PhotoDownloadViewModel
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.rememberUserContainerUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.error.ErrorContent
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserContainer
import com.goforer.phogal.presentation.ui.theme.Black
import com.goforer.phogal.presentation.ui.theme.Blue75
import com.goforer.phogal.presentation.ui.theme.ColorBlackLight
import com.goforer.phogal.presentation.ui.theme.ColorSnowWhite
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray1
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray5
import com.goforer.phogal.presentation.ui.compose.base.designsystem.component.shimmer
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.theme.ColorSystemGray7
import com.goforer.phogal.presentation.ui.theme.ColorText4
import com.goforer.phogal.presentation.ui.theme.DarkGreen60
import kotlinx.coroutines.launch

@Immutable
sealed interface DownloadDialogState {
    object Idle : DownloadDialogState
    object Success : DownloadDialogState
    object Duplicate : DownloadDialogState
    data class Error(val message: String) : DownloadDialogState
}

@Composable
fun PictureViewerContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    state: PhotoContentUiState = rememberPhotoContentUiState(),
    pictureViewModel: PictureViewModel = hiltViewModel(),
    photoDownloadViewModel: PhotoDownloadViewModel = hiltViewModel(),
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onShownPhoto: (pictureUiState: Picture) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit
) {
    val pictureState by pictureViewModel.picture.collectAsStateWithLifecycle()
    val trackDownloadState by photoDownloadViewModel.trackDownload.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        PictureBody(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            pictureState = pictureState,
            visibleViewButton = state.visibleViewButton,
            onViewPhotos = onViewPhotos,
            onShowSnackBar = onShowSnackBar,
            onShownPhoto = onShownPhoto,
            onOpenWebView = onOpenWebView,
            onSuccess = onSuccess,
            onClick = { url ->
                photoDownloadViewModel.getDownloadPhotoUrl(url)
                state.setShowPopup(true)
            }
        )

        if (state.showPopup) {
            LoadingIndicator()
        }

        DownloadPhoto(
            trackDownloadState = trackDownloadState,
            showPopup = state.showPopup,
            onRetry = { pictureViewModel.loadPicture(state.id) },
            onDismissPopup = { state.setShowPopup(false)},
            onDownload = { url ->
                state.baseUiState.scope.launch {
                    photoDownloadViewModel.downloadPhoto(url, state.id)
                        .onSuccess {
                            state.setDialogState(DownloadDialogState.Success)
                            state.setShowPopup(false)
                        }
                        .onFailure { error ->
                            state.setDialogState(
                                if (error is PhotoAlreadyExistsException) {
                                    DownloadDialogState.Duplicate
                                } else {
                                    DownloadDialogState.Error(
                                        error.message ?: state.baseUiState.context.getString(R.string.error_unknown)
                                    )
                                }
                            )
                            state.setShowPopup(false)
                        }
                }
            }
        )
    }

    ShowDialog(
        dialogState = state.dialogState,
        onDismiss = {
            state.setDialogState(DownloadDialogState.Idle)
            state.setShowPopup(false)
        },
        onDismissRequest = {
            state.setDialogState(DownloadDialogState.Idle)
            state.setShowPopup(false)
        },
    )
}

@Composable
fun PictureBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    pictureState: UiState<Picture>,
    visibleViewButton: Boolean,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onShownPhoto: (pictureUiState: Picture) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit,
    onClick: (id: String) -> Unit
) {
    when (pictureState) {
        is UiState.Success -> {
            val picture = pictureState.data

            onSuccess(true)
            LaunchedEffect(picture.id) { onShownPhoto(picture) }
            PictureBodyContent(
                modifier = modifier,
                contentPadding = contentPadding,
                picture = picture,
                visibleViewButton = visibleViewButton,
                onViewPhotos = onViewPhotos,
                onShowSnackBar = onShowSnackBar,
                onShownPhoto = onShownPhoto,
                onOpenWebView = onOpenWebView,
                onClick = onClick,
            )
        }
        UiState.Loading, UiState.Idle -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                ) {
                    LoadingPicture(
                        modifier = Modifier.padding(4.dp, 4.dp),
                        enableLoadIndicator = true
                    )
                }
            }
        }
        is UiState.Error -> {
            onSuccess(false)
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                ) {
                    ErrorContent(
                        modifier = Modifier,
                        title = if (pictureState.code !in 200..299)
                            stringResource(id = R.string.error_dialog_network_title)
                        else
                            stringResource(id = R.string.error_dialog_title),
                        message = "${stringResource(id = R.string.error_get_picture)}${"\n\n"}${pictureState.message}",
                        onRetry = {
                            //pictureViewModel.loadPicture(state.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadPhoto(
    trackDownloadState: UiState<TrackDownload>,
    showPopup: Boolean,
    onRetry: () -> Unit,
    onDismissPopup: () -> Unit,
    onDownload: (url: String) -> Unit
) {
    LaunchedEffect(trackDownloadState) {
        if (trackDownloadState is UiState.Success) {
            onDownload(trackDownloadState.data.url)
        }
    }

    if (trackDownloadState is UiState.Error && showPopup) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(),
                exit = scaleOut(transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Card(
                    modifier = Modifier
                        .padding(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDismissPopup()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    ErrorContent(
                        modifier = Modifier.padding(16.dp),
                        isFullMaxSize = false,
                        title = if (trackDownloadState.code !in 200..299)
                            stringResource(id = R.string.error_dialog_network_title)
                        else
                            stringResource(id = R.string.error_dialog_title),
                        message = "${stringResource(id = R.string.error_get_picture)}\n\n${trackDownloadState.message}",
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
fun PictureBodyContent(
    modifier: Modifier,
    contentPadding: PaddingValues,
    picture: Picture,
    visibleViewButton: Boolean,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onShownPhoto: (picture: Picture) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onClick: (id: String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyContent(
                modifier = Modifier,
                picture = picture,
                visibleViewPhotosButton = visibleViewButton,
                onViewPhotos = onViewPhotos,
                onShowSnackBar = onShowSnackBar,
                onShownPhoto = onShownPhoto,
                onOpenWebView = onOpenWebView,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ShowDialog(
    dialogState: DownloadDialogState,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit
) {
    when (dialogState) {
        is DownloadDialogState.Success -> {
            AutoDismissDialog(
                message = stringResource(id = R.string.picture_download_complete),
                visible = true,
                onDismiss = onDismiss
            )
        }

        is DownloadDialogState.Duplicate -> {
            AlertDialog(
                title = stringResource(id = R.string.picture_download_notification),
                message = stringResource(id = R.string.picture_name_exist),
                onDismissRequest = onDismissRequest
            )
        }

        is DownloadDialogState.Error -> {
            AlertDialog(
                title = "Download Failed",
                message = dialogState.message,
                onDismissRequest = onDismissRequest
            )
        }

        DownloadDialogState.Idle -> { /* Noting */ }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp,
                    color = Color.Magenta,
                    trackColor = Color.LightGray,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.picture_download_indicator),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

/*
@Composable
fun DownloadLoadingIndicatorBox(context: Context, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress_rate"
    )

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(100.dp),
            strokeWidth = 8.dp,
            color = Color.Magenta,
            trackColor = Color.LightGray,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedButton(
        onClick = {
            /*
            photoDownloadViewModel.cancelDownload(
                context.getString(R.string.error_download_canceled)
            )
             */
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.LightGray,
            contentColor = Color.Red
        )
    ) {
        Text(
            text = context.getString(R.string.cancel),
            color = Color.Red
        )
    }
}

 */

@Composable
fun BodyContent(
    modifier: Modifier = Modifier,
    picture: Picture,
    visibleViewPhotosButton: Boolean,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShowSnackBar: (text: String) -> Unit,
    onShownPhoto: (picture: Picture) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onClick: (id: String) -> Unit
) {
    var visibleCameraInfo by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.padding(0.dp, 2.dp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor =  MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
            focusedElevation = 4.dp
        ),
        shape = RectangleShape
    ) {
        val imageUrl = picture.urls.raw
        val painter = loadImagePainter(
            data = imageUrl,
            size = Size(picture.width.div(8), picture.height.div(8))
        )

        if (painter.state is AsyncImagePainter.State.Loading) {
            val screenHeight = LocalWindowInfo.current.containerSize
            val holderModifier = Modifier
                .fillMaxWidth()
                .height(screenHeight.height.dp)
                .align(Alignment.CenterHorizontally)
                .background(ColorSystemGray7)
                .shimmer(
                    baseColor = ColorSystemGray7,
                    highlightColor = MaterialTheme.colorScheme.surface,
                )

            Text(
                modifier = holderModifier,
                text = "",
                textAlign = TextAlign.Center
            )
        } else {
            UserContainer(
                modifier = Modifier,
                state = rememberUserContainerUiState(
                    user = rememberSaveable { mutableStateOf(picture.user.toString()) },
                    profileSize = rememberSaveable { mutableDoubleStateOf(48.0) },
                    colors = remember { mutableStateOf(listOf(ColorSystemGray1, ColorSystemGray1, ColorSnowWhite, ColorSystemGray5, Blue75, DarkGreen60)) },
                    visibleViewButton = rememberSaveable { mutableStateOf(visibleViewPhotosButton) },
                    fromItem = rememberSaveable { mutableStateOf(false) }
                ),
                onViewPhotos = onViewPhotos,
                onShowSnackBar = onShowSnackBar,
                onOpenWebView = onOpenWebView
            )

            AnimatedVisibility(
                visible = true,
                modifier = modifier,
                enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                        fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                        fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
            ) {
                ImageContent(
                    painter = painter,
                    onClick = {
                        onClick(picture.id)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            BehaviorItem(700, 700, 700)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = picture.description ?: "None",
                modifier = Modifier.padding(8.dp, 4.dp),
                color = ColorText4,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.W400,
                fontSize = 18.sp,
                fontStyle = FontStyle.Normal,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            picture.location?.let {
                LocationItem(it.name)
                Spacer(modifier = Modifier.height(2.dp))
            }

            DateItem(picture.createdAt)
            Spacer(modifier = Modifier.height(2.dp))
            picture.exif?.let { exif ->
                GenericCubicAnimationShape(
                    visible = visibleCameraInfo,
                    duration = 550
                ) { animatedShape, _ ->
                    ExifItem(
                        modifier = modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .graphicsLayer {
                                clip = true
                                shape = animatedShape
                            },
                        exifUiState = exif
                    )
                }
            }
            IconButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                height = 32.dp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue75,
                    contentColor = Color.White
                ),
                onClick = {
                    visibleCameraInfo = !visibleCameraInfo
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(
                        text = if (visibleCameraInfo)
                            stringResource(id = R.string.picture_close_camera_info)
                        else
                            stringResource(id = R.string.picture_view_camera_info),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            )

            Spacer(modifier = Modifier.height(70.dp))
            onShownPhoto(picture)
        }
    }
}

@Composable
fun ImageContent(
    modifier: Modifier = Modifier,
    painter: AsyncImagePainter,
    onClick: () -> Unit
) {
    val transition by animateFloatAsState(
        targetValue = if (painter.state is AsyncImagePainter.State.Success) 1f else 0f
    )

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .then(
                ((painter.state as? AsyncImagePainter.State.Success)
                    ?.painter
                    ?.intrinsicSize
                    ?.let { intrinsicSize ->
                        Modifier.aspectRatio(intrinsicSize.width / intrinsicSize.height)
                    } ?: Modifier)
            )
            .clip(RectangleShape)
            .clickable {
                onClick()
            }
            .scale(.8f + (.2f * transition))
            .graphicsLayer { rotationX = (1f - transition) * 5f }
            .alpha(transition / .2f),
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
            setToSaturation(
                transition
            )
        })
    )
}

@Composable
fun BehaviorItem(likes: Long, downloads: Long, views: Long) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${likes}${" "}${stringResource(id = R.string.picture_likes)}",
            modifier = Modifier.padding(vertical =  4.dp),
            color = Black,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            fontStyle = FontStyle.Normal,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${downloads}${" "}${stringResource(id = R.string.picture_downloads)}",
            modifier = Modifier.padding(vertical = 4.dp),
            color = Black,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            fontStyle = FontStyle.Normal,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${views}${" "}${stringResource(id = R.string.picture_views)}",
            modifier = Modifier.padding(vertical = 4.dp),
            color = Black,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            fontStyle = FontStyle.Normal,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun LocationItem(location: String?) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_location),
            contentDescription = "Location",
            modifier = Modifier
                .size(22.dp)
                .padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = location ?: stringResource(id = R.string.picture_no_location),
            color = ColorBlackLight,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun DateItem(createdAt: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_date),
            contentDescription = "Date",
            modifier = Modifier
                .size(22.dp)
                .padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${createdAt}${" "}${stringResource(id = R.string.picture_posted)}",
            color = ColorBlackLight,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ExifItem(
    modifier: Modifier = Modifier,
    exifUiState: Exif
) {
    Box(modifier) {
        Column {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "ExifUiState",
                    modifier = Modifier
                        .size(22.dp)
                        .padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = exifUiState.name ?: stringResource(id = R.string.picture_no_camera_name),
                    color = ColorBlackLight,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            exifUiState.name?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${"Lens\n"}${"f/"}${exifUiState.aperture}${"  "}${exifUiState.focalLength}${"mm  "}${exifUiState.exposureTime}${"s  iso "}${exifUiState.iso}",
                    modifier = Modifier.padding(horizontal = 28.dp),
                    color = ColorBlackLight,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}