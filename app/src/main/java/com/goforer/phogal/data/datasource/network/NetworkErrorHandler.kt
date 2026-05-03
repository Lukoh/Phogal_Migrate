package com.goforer.phogal.data.datasource.network

import android.content.Context
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkErrorHandler
@Inject
constructor(
    val context: Context,
    private val json: Json
) {
    internal fun handleError(errorMessage: String) {
        runCatching {
            val networkError = json.decodeFromString(NetworkError.serializer(), errorMessage)
            networkError.detail.firstOrNull()?.type?.let {
                when (it) {
                    "INVALID_SESSION" -> {
                    }
                    "OBSOLETE_VERSION" -> {
                    }
                }
            }
        }.onFailure { e ->
            Timber.d("Exception $e")
        }
    }
}
