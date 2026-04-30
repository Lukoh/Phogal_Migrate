package com.goforer.phogal.data.datasource.network

import kotlinx.serialization.Serializable

/**
 * Server-side error envelope from the Unsplash API for non-2xx responses.
 *
 * The shape is `{ "detail": [ { loc, msg, type }, ... ] }`. We always read at
 * least `detail[0]`, so callers should `.firstOrNull()` if they need to be
 * defensive.
 *
 * ### Migrated to kotlinx.serialization (April 2026)
 *
 * Both this class and its inner `ErrorBody` are annotated `@Serializable`.
 * `@Serializable` is a compile-time annotation handled by the Kotlin
 * Serialization plugin — there is no reflection at runtime, and any field
 * the server adds in the future will be tolerated as long as the project's
 * `Json` instance is configured with `ignoreUnknownKeys = true`
 * (it is, in `AppModule.provideJson()`).
 */
@Serializable
data class NetworkError(val detail: List<ErrorBody>) {

    /**
     * One error entry. The Unsplash API returns at least one entry per error
     * response. `msg` is `var` because the request interceptor in
     * `AppModule.getRequestInterceptor` mutates it to prepend the URL path
     * — that behavior pre-dates this migration and is preserved as-is.
     */
    @Serializable
    data class ErrorBody(
        val loc: List<String>,
        var msg: String = ERROR_DEFAULT,
        val type: String
    )

    companion object {
        const val ERROR_DEFAULT = "An unexpected error has occurred"

        const val ERROR_SERVICE_UNAVAILABLE = 503
        const val ERROR_SERVICE_BAD_GATEWAY = 502

        const val ERROR_SERVICE_UNPROCESSABLE_ENTITY = 422
    }
}
