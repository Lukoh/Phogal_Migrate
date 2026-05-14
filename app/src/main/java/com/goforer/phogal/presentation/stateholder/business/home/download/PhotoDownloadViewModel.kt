package com.goforer.phogal.presentation.stateholder.business.home.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.phogal.R
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.PhotoDownload
import com.goforer.phogal.data.repository.download.DownloadStatus
import com.goforer.phogal.data.repository.download.PhotoDownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PhotoDownloadViewModel @Inject constructor(
    private val photoDownloadRepository: PhotoDownloadRepository
) : ViewModel() {
    private val _downloadPhoto = MutableStateFlow(PhotoDownload.empty())
    val downloadPhoto: StateFlow<PhotoDownload> = _downloadPhoto.asStateFlow()

    private var downloadJob: Job? = null

    fun startDownload(url: String, fileName: String) {
        downloadJob?.cancel()

        downloadJob = viewModelScope.launch {
            _downloadPhoto.update {
                it.copy(isDownloading = true, progress = 0f, errorMessage = null, savedFile = null)
            }

            photoDownloadRepository.downloadPhoto(url, fileName).collect { status ->
                when (status) {
                    is DownloadStatus.Progress -> {
                        _downloadPhoto.update { it.copy(progress = status.percentage) }
                    }
                    is DownloadStatus.Success -> {
                        _downloadPhoto.update { it.copy(isDownloading = false, savedFile = status.file) }
                    }
                    is DownloadStatus.Error -> {
                        _downloadPhoto.update { it.copy(isDownloading = false, errorMessage = status.message) }
                    }
                }
            }
        }
    }

    fun cancelDownload(message: String) {
        downloadJob?.cancel()
        _downloadPhoto.update {
            it.copy(isDownloading = false, progress = 0f, errorMessage = message)
        }
    }
}