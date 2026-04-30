package com.goforer.phogal.presentation.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for Phogal — **Navigation 3 1.1.0 only**.
 *
 * Every destination is a `@Serializable` class/object implementing [NavKey].
 * In Nav3, [NavKey] is the single source of identity for an entry on the
 * back stack: the same instance that was pushed is delivered back (typed)
 * into the entry lambda — no `.toRoute()` conversion, no string routes.
 *
 * ## Pushing / popping
 *  - Push:  `backStack.add(PictureRoute(id = "abc", showViewPhotosButton = true))`
 *  - Pop :  `backStack.removeLastOrNull()`
 *
 * ## Reading parameters inside an entry
 *   ```
 *   entry<PictureRoute> { key -> key.id }
 *   ```
 *
 * ## Why there are no `*Graph` roots anymore
 * Nav3 has no nested-graph concept. Tab identity in Phogal is carried by
 * [com.goforer.phogal.presentation.ui.compose.screen.home.BottomNavRoute]
 * (a sealed interface that itself implements [NavKey]) and the per-tab
 * `NavBackStack` in
 * [com.goforer.phogal.presentation.ui.navigation.nav3.NavigationState]. The
 * Nav2-era `GalleryGraph` / `PopularPhotosGraph` / `NotificationGraph` /
 * `SettingGraph` data objects have been removed because they were
 * `navigation<T>(...)` graph roots — a Nav2-only concept with no counterpart
 * in Nav3.
 */
object Routes {

    // ─────────────────────────────── Gallery tab screens ─────────────────────────────────

    @Serializable data object SearchPhotosRoute : NavKey

    /**
     * Dialog destination for the "storage permission" bottom sheet.
     *
     * This is pushed onto the back stack and rendered by [androidx.navigation3.scene.DialogSceneStrategy]
     * so it behaves like a first-class navigation entry: predictive back works,
     * rotation is preserved, and process death restores it.
     */
    @Serializable data object PermissionDialogRoute : NavKey

    @Serializable data class PictureRoute(
        val id: String,
        val showViewPhotosButton: Boolean
    ) : NavKey

    @Serializable data class UserPhotosRoute(
        val name: String,
        val firstName: String,
        val lastName: String,
        val username: String? = null
    ) : NavKey

    @Serializable data class WebViewRoute(
        val firstName: String,
        val url: String
    ) : NavKey

    // ────────────────────────────── Popular photos tab ───────────────────────────────────

    @Serializable data object PopularPhotosRoute : NavKey

    // ────────────────────────────── Notification tab ─────────────────────────────────────

    @Serializable data object NotificationsRoute : NavKey

    @Serializable data class NotificationRoute(val id: String) : NavKey

    // ─────────────────────────────── Setting tab screens ─────────────────────────────────

    @Serializable data object SettingRoute : NavKey
    @Serializable data object BookmarkedPhotosRoute : NavKey
    @Serializable data object FollowingUsersRoute : NavKey
    @Serializable data object NotificationSettingRoute : NavKey
}
