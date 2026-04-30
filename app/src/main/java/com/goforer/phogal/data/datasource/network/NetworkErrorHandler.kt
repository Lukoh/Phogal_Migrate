package com.goforer.phogal.data.datasource.network

import android.content.Context
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decodes server-side error envelopes and dispatches based on the error type.
 *
 * The decoding contract: input is a JSON string in the shape declared by
 * [NetworkError], and we route on the first entry's `type` field.
 *
 * ### Migrated to kotlinx.serialization (April 2026)
 *
 * The previous implementation used `Gson().fromJson(...)` per call, which
 * created a fresh `Gson` instance on every error — small inefficiency, but
 * also meant the parsing rules diverged from the rest of the app. Now we
 * inject the project's shared [Json] instance, so all four loci that decode
 * JSON (Retrofit responses, error envelopes here, persisted local data,
 * one-off conversions) use identical configuration.
 */
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
