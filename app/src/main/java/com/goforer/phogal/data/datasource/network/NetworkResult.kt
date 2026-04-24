package com.goforer.phogal.data.datasource.network

/**
 * Type-safe result wrapper for network responses.
 *
 * Replaces the legacy mutable `Resource` class. A `NetworkResult` is one of:
 *  - [Success]   : HTTP 2xx with a non-null body
 *  - [Empty]     : HTTP 204 / empty body (successful, no payload)
 *  - [Error]     : HTTP 4xx/5xx with status code and error message
 *  - [Exception] : Network / IO / serialization failure before any HTTP response was produced
 *
 * Consumers should prefer exhaustive `when` over null checks or `is` casts to `Any`.
 */
sealed interface NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>

    object Empty : NetworkResult<Nothing>

    data class Error(
        val code: Int,
        val message: String
    ) : NetworkResult<Nothing>

    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>

    companion object {
        /** Convenience: is this a terminal failure state (either [Error] or [Exception])? */
        fun NetworkResult<*>.isFailure(): Boolean = this is Error || this is Exception
    }
}

/**
 * Maps the success payload of a [NetworkResult] to another type, leaving failure states untouched.
 */
inline fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Empty -> NetworkResult.Empty
    is NetworkResult.Error -> this
    is NetworkResult.Exception -> this
}
