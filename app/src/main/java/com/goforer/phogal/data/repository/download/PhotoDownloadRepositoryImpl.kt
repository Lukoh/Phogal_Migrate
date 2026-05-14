package com.goforer.phogal.data.repository.download

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.goforer.phogal.R
import com.goforer.phogal.data.datasource.network.api.RestAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoDownloadRepositoryImpl @Inject constructor(
    private val api: RestAPI,
    private val context: Context
) : PhotoDownloadRepository {
    override fun downloadPhoto(url: String, fileName: String): Flow<DownloadStatus> = flow {
        try {
            val response = api.downloadPhoto(url)
            val body = response.body() ?: throw Exception(context.getString(R.string.error_unknown))
            val contentLength = body.contentLength()
            val file = File(context.filesDir, "$fileName.png")

            body.byteStream().use { input ->
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var bytesRead: Long = 0

                while (true) {
                    currentCoroutineContext().ensureActive()

                    val read = input.read(buffer)

                    if (read == -1) break

                    outputStream.write(buffer, 0, read)
                    bytesRead += read

                    if (contentLength > 0) {
                        emit(DownloadStatus.Progress(bytesRead.toFloat() / contentLength))
                    }
                }

                val bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())

                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                emit(DownloadStatus.Success(file))
            }
        } catch (e: Exception) {
            emit(DownloadStatus.Error(e.message ?: context.getString(R.string.error_unknown)))
        }
    }.flowOn(Dispatchers.IO)
}