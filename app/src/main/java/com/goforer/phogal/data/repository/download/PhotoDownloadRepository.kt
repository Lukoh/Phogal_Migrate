package com.goforer.phogal.data.repository.download

import kotlinx.coroutines.flow.Flow
import java.io.File

sealed class DownloadStatus {
    data class Progress(val percentage: Float) : DownloadStatus()
    data class Success(val file: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

interface PhotoDownloadRepository {
    fun downloadPhoto(url: String, fileName: String): Flow<DownloadStatus>
}