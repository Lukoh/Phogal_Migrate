package com.goforer.phogal.presentation.stateholder.business.home.setting.notification

import androidx.lifecycle.ViewModel
import com.goforer.phogal.data.datasource.local.LocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Tiny settings VM — just a typed shim around [LocalDataSource] preference flags.
 *
 * The legacy version inherited from a network-aware base class for no reason; we no
 * longer do that. If you ever need to drive UI from these preferences reactively,
 * migrate them to a `StateFlow` — right now they're imperative one-shot reads to
 * match the existing SettingScreen.
 */
@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val localDataSource: LocalDataSource
) : ViewModel() {

    enum class NotificationChannel {
        Following, Latest, Community
    }

    fun setEnabled(channel: NotificationChannel, enabled: Boolean) {
        when (channel) {
            NotificationChannel.Following -> localDataSource.enabledFollowingNotification = enabled
            NotificationChannel.Latest    -> localDataSource.enabledLatestNotification = enabled
            NotificationChannel.Community -> localDataSource.enableCommunityNotification = enabled
        }
    }

    fun isEnabled(channel: NotificationChannel): Boolean = when (channel) {
        NotificationChannel.Following -> localDataSource.enabledFollowingNotification
        NotificationChannel.Latest    -> localDataSource.enabledLatestNotification
        NotificationChannel.Community -> localDataSource.enableCommunityNotification
    }

    fun setNotificationEnabled(toggled: Boolean) {}
    fun getNotificationSetting(): Boolean {
        return true
    }
}
