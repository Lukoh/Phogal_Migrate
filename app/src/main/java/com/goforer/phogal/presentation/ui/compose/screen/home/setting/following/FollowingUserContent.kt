package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

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
import com.goforer.phogal.data.model.remote.response.gallery.common.User
import com.goforer.phogal.presentation.stateholder.business.home.common.follow.FollowViewModel
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen

@Composable
fun FollowingUsersContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    followViewModel: FollowViewModel = hiltViewModel(),
    enabledLoadPhotosState: MutableState<Boolean>,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String?) -> Unit
) {
    val users = followViewModel.followingUsers.collectAsStateWithLifecycle().value

    if (users.isNotEmpty()) {
        FollowingUsersSection(
            modifier = modifier,
            contentPadding = contentPadding,
            users = users as MutableList<User>,
            onViewPhotos = onViewPhotos,
            onOpenWebView = onOpenWebView,
            onFollow = {
                followViewModel.setUserFollow(it)
            }
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
                    text = stringResource(id = R.string.setting_no_following)
                )
            }
        }
    }
}