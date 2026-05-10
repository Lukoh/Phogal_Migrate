package com.goforer.phogal.presentation.ui.navigation.nav3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.goforer.base.customtab.openCustomTab
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.business.home.common.user.UserPhotosViewModel
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PictureScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos.UserPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.common.webview.WebViewScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.SearchPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.notifcation.notifications.NotificationsScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos.PopularPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.SettingScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark.BookmarkedPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.following.FollowingUsersScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.notification.NotificationSettingScreen
import com.goforer.phogal.presentation.ui.navigation.Routes

fun EntryProviderScope<NavKey>.phogalEntries(navState: NavigationState) {
    galleryTabEntries(navState)
    popularTabEntries(navState)
    notificationTabEntries(navState)
    settingTabEntries(navState)
}

// ─────────────────────────────── Gallery tab ───────────────────────────────

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun EntryProviderScope<NavKey>.galleryTabEntries(navigationState: NavigationState) {

    // SearchPhotos is the LIST pane. On tablets/foldables it occupies the
    // left column; on phones it renders full-screen with the detail pushed
    // on top when an item is tapped.
    entry<Routes.SearchPhotosRoute>(
        metadata = ListDetailSceneStrategy.listPane(
            detailPlaceholder = { DetailPlaceholder() }
        )
    ) {
        val galleryViewModel: GalleryViewModel = hiltViewModel()
        SearchPhotosScreen(
            galleryViewModel = galleryViewModel,
            contentUiState = rememberSearchPhotosContentUiState(
                baseUiState = rememberBaseUiState(), galleryViewModel = galleryViewModel
            ),
            onItemClicked = { id ->
                navigationState.push(Routes.PictureRoute(id = id, showViewPhotosButton = true))
            },
            onViewPhotos = { name, firstName, lastName, username ->
                navigationState.push(
                    Routes.UserPhotosRoute(
                        name = name,
                        firstName = firstName,
                        lastName = lastName,
                        username = username
                    )
                )
            },
            onOpenWebView = { firstName, url ->
                navigationState.push(Routes.WebViewRoute(firstName = firstName, url = url))
            }
        )
    }

    // PictureRoute is the DETAIL pane — paired with SearchPhotos on wide screens.
    entry<Routes.PictureRoute>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { key ->
        val pictureViewModel: PictureViewModel = hiltViewModel()
        PictureScreen(
            pictureViewModel = pictureViewModel,
            state = rememberPhotoContentUiState(
                id = rememberSaveable { mutableStateOf(key.id) },
                visibleViewButton = rememberSaveable {
                    mutableStateOf(key.showViewPhotosButton)
                }
            ),
            onViewPhotos = { name, firstName, lastName, username ->
                navigationState.push(
                    Routes.UserPhotosRoute(
                        name = name,
                        firstName = firstName,
                        lastName = lastName,
                        username = username
                    )
                )
            },
            onBackPressed = { navigationState.pop() },
            onOpenWebView = { firstName, url ->
                navigationState.push(Routes.WebViewRoute(firstName = firstName, url = url))
            }
        )
    }

    entry<Routes.UserPhotosRoute> { key ->
        val userPhotosViewModel: UserPhotosViewModel = hiltViewModel()
        UserPhotosScreen(
            userPhotosViewModel = userPhotosViewModel,
            contentUiState = rememberUserPhotosContentUiState(
                baseUiState = rememberBaseUiState(),
                name = rememberSaveable { mutableStateOf(key.name) },
                firstName = rememberSaveable { mutableStateOf(key.firstName) }
            ),
            onItemClicked = { id ->
                navigationState.push(Routes.PictureRoute(id = id, showViewPhotosButton = false))
            },
            onBackPressed = { navigationState.pop() }
        )
    }

    entry<Routes.WebViewRoute> { key ->
        WebViewScreen(
            firstName = key.firstName,
            url = key.url,
            onBackPressed = { navigationState.pop() }
        )
    }

    // Storage-permission prompt as a backstack-managed Dialog scene.
    // DialogSceneStrategy routes this entry into a real `Dialog` instance, so
    // predictive back / rotation / process-death restoration all work.
    //
    // Note: we intentionally do NOT reuse `PermissionBottomSheet` here — it
    // wraps its content in a Material `ModalBottomSheet`, which would nest
    // inside the Dialog provided by DialogSceneStrategy (two overlays stacked).
    // Instead, the dialog entry renders a lightweight inline UI. If you want a
    // bottom-sheet feel, migrate the shared content out of PermissionBottomSheet
    // into a separate `PermissionRequestContent` composable that both this
    // entry and the legacy bottom sheet can call.
    entry<Routes.PermissionDialogRoute>(
        metadata = DialogSceneStrategy.dialog()
    ) {
        PermissionDialogContent(
            onDismiss = { navigationState.pop() },
            onConfirm = { navigationState.pop() }
        )
    }
}

// ───────────────────────── Popular photos tab ─────────────────────────

private fun EntryProviderScope<NavKey>.popularTabEntries(navState: NavigationState) {
    entry<Routes.PopularPhotosRoute> {
        PopularPhotosScreen(
            onItemClicked = { id ->
                navState.push(Routes.PictureRoute(id = id, showViewPhotosButton = true))
            },
            onViewPhotos = { name, firstName, lastName, username ->
                navState.push(
                    Routes.UserPhotosRoute(
                        name = name,
                        firstName = firstName,
                        lastName = lastName,
                        username = username
                    )
                )
            },
            onOpenWebView = { firstName, url ->
                navState.push(Routes.WebViewRoute(firstName = firstName, url = url))
            }
        )
    }
}

// ───────────────────────── Notification tab ─────────────────────────

private fun EntryProviderScope<NavKey>.notificationTabEntries(navState: NavigationState) {
    entry<Routes.NotificationsRoute> {
        NotificationsScreen(
            onItemClicked = { id ->
                navState.push(Routes.NotificationRoute(id = id))
            }
        )
    }

    entry<Routes.NotificationRoute> {
        NotificationsScreen(
            onItemClicked = { navState.push(Routes.NotificationRoute(it)) }
        )
    }
}

// ─────────────────────────── Setting tab ───────────────────────────

private fun EntryProviderScope<NavKey>.settingTabEntries(navState: NavigationState) {
    entry<Routes.SettingRoute> {
        SettingScreen(
            baseUiState = rememberBaseUiState(),
            onItemClicked = { context, index ->
                when (index) {
                    0 -> navState.push(Routes.BookmarkedPhotosRoute)
                    1 -> navState.push(Routes.FollowingUsersRoute)
                    2 -> navState.push(Routes.NotificationSettingRoute)
                    4 -> openCustomTab(context, "https://lukoh.github.io/Phogal/")
                    7 -> openCustomTab(context, "https://github.com/Lukoh/Phogal")
                    else -> Unit
                }
            }
        )
    }

    entry<Routes.BookmarkedPhotosRoute> {
        BookmarkedPhotosScreen(
            onItemClicked = { picture, _ ->
                navState.push(
                    Routes.PictureRoute(id = picture.id, showViewPhotosButton = false)
                )
            },
            onBackPressed = { navState.pop() },
            onViewPhotos = { name, firstName, lastName, username ->
                navState.push(
                    Routes.UserPhotosRoute(
                        name = name,
                        firstName = firstName,
                        lastName = lastName,
                        username = username
                    )
                )
            },
            onOpenWebView = { firstName, url ->
                navState.push(Routes.WebViewRoute(firstName = firstName, url = url))
            }
        )
    }

    entry<Routes.FollowingUsersRoute> {
        FollowingUsersScreen(
            onBackPressed = { navState.pop() },
            onViewPhotos = { name, firstName, lastName, username ->
                navState.push(
                    Routes.UserPhotosRoute(
                        name = name,
                        firstName = firstName,
                        lastName = lastName,
                        username = username
                    )
                )
            },
            onOpenWebView = { firstName, url ->
                navState.push(Routes.WebViewRoute(firstName = firstName, url = url))
            }
        )
    }

    entry<Routes.NotificationSettingRoute> {
        NotificationSettingScreen(onBackPressed = { navState.pop() })
    }
}

// ─────────────────────────── Helpers ───────────────────────────

/**
 * Placeholder shown in the right (detail) pane of the list-detail layout
 * when no photo is selected. Only visible on wide screens / tablets —
 * on compact phones the list pane occupies the whole screen until the
 * user taps an item.
 */
@Composable
private fun DetailPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.bottom_navigation_photo),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Content for the permission-prompt dialog entry. Rendered inside the
 * `Dialog` instance provided by [DialogSceneStrategy], so we do NOT wrap it
 * in another dialog/bottom-sheet. Keep this lightweight — its job is to
 * notify the user what permission is needed and let them confirm or cancel.
 *
 * For a full bottom-sheet UX, use the existing non-Nav3 `PermissionBottomSheet`
 * directly inside `SearchPhotosScreen`. This dialog-scene variant is here as
 * an example of how any ad-hoc dialog can be made a first-class back-stack
 * entry in Nav3 1.1.0.
 */
@Composable
private fun PermissionDialogContent(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.bottom_navigation_photo))
        },
        text = {
            Text(text = stringResource(id = R.string.bottom_navigation_photo))
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
