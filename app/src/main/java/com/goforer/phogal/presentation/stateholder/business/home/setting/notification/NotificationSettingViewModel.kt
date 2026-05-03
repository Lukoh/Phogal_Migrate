package com.goforer.phogal.presentation.stateholder.business.home.setting.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.phogal.data.datasource.local.LocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val localDataSource: LocalDataSource
) : ViewModel() {

    enum class NotificationChannel {
        Following, Latest, Community
    }

    suspend fun setEnabled(channel: NotificationChannel, enabled: Boolean) {
        when (channel) {
            NotificationChannel.Following -> localDataSource.setFollowingNotificationEnabled(enabled)
            NotificationChannel.Latest    -> localDataSource.setLatestNotificationEnabled(enabled)
            NotificationChannel.Community -> localDataSource.setCommunityNotificationEnabled(enabled)
        }
    }

    fun isEnabled(channel: NotificationChannel): Boolean = when (channel) {
        NotificationChannel.Following -> localDataSource.enabledFollowingNotificationFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value
        NotificationChannel.Latest    -> localDataSource.enabledLatestNotificationFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value
        NotificationChannel.Community -> localDataSource.enableCommunityNotificationFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value
    }

    fun setNotificationEnabled(toggled: Boolean) {}
    fun getNotificationSetting(): Boolean {
        return true
    }
}
