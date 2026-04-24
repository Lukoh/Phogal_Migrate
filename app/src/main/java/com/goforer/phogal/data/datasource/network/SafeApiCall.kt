package com.goforer.phogal.data.datasource.network

import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a Retrofit suspend call and maps the result into a [NetworkResult].
 *
 * Rules:
 *  - HTTP 204 or a null body on a 2xx response produces [NetworkResult.Empty].
 *  - HTTP 2xx with a non-null body produces [NetworkResult.Success].
 *  - HTTP non-2xx produces [NetworkResult.Error] with code + best-effort error message.
 *  - [IOException] (connectivity, timeouts) and any other thrown [Throwable] produce
 *    [NetworkResult.Exception].
 *  - [CancellationException] is re-thrown so structured concurrency keeps working;
 *    it is never swallowed.
 */
suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null || response.code() == 204) {
                NetworkResult.Empty
            } else {
                NetworkResult.Success(body)
            }
        } else {
            val errorMessage = runCatching { response.errorBody()?.string() }.getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: response.message()
                ?: "Unknown HTTP error"
            NetworkResult.Error(code = response.code(), message = errorMessage)
        }
    } catch (ce: CancellationException) {
        throw ce
    } catch (httpException: HttpException) {
        Timber.w(httpException, "safeApiCall HttpException")
        NetworkResult.Error(
            code = httpException.code(),
            message = httpException.message ?: "HTTP ${httpException.code()}"
        )
    } catch (ioException: IOException) {
        Timber.w(ioException, "safeApiCall IOException")
        NetworkResult.Exception(ioException)
    } catch (throwable: Throwable) {
        Timber.e(throwable, "safeApiCall unexpected failure")
        NetworkResult.Exception(throwable)
    }
}
