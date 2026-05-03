package com.goforer.phogal.di.module

import android.app.Application
import android.content.Context
import android.os.Build
import com.orhanobut.logger.Logger
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.goforer.base.extension.isNull
import com.goforer.base.utils.connect.ConnectivityManagerNetworkMonitor
import com.goforer.phogal.BuildConfig
import com.goforer.phogal.data.datasource.network.NetworkError
import com.goforer.phogal.data.datasource.network.NetworkErrorHandler
import com.goforer.phogal.data.datasource.network.adapter.factory.NullOnEmptyConverterFactory
import com.goforer.phogal.data.datasource.network.api.RestAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val timeout_read = 60L
    private const val timeout_connect = 60L
    private const val timeout_write = 60L

    @Singleton
    @Provides
    fun appContext(application: Application): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideConnectivityManagerNetworkMonitor(context: Context) = ConnectivityManagerNetworkMonitor(context)

    @Singleton
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    @Singleton
    @Provides
    fun provideNetworkErrorHandler(context: Context, json: Json) = NetworkErrorHandler(context, json)

    @Singleton
    @Provides
    fun providePersistentCookieJar(context: Context) =
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptor: Interceptor,
        cookieJar: PersistentCookieJar
    ): OkHttpClient {
        val ok = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(timeout_connect, TimeUnit.SECONDS)
            .readTimeout(timeout_read, TimeUnit.SECONDS)
            .writeTimeout(timeout_write, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor =
                HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        if (isJSONValid(message))
                            Logger.json(message)
                        else
                            Timber.d("%s", message)
                    }

                    fun isJSONValid(jsonInString: String): Boolean {
                        try {
                            JSONObject(jsonInString)
                        } catch (ex: JSONException) {
                            try {
                                JSONArray(jsonInString)
                            } catch (ex1: JSONException) {
                                return false
                            }

                        }

                        return true
                    }
                })

            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            ok.addInterceptor(httpLoggingInterceptor)
        }

        ok.addInterceptor(interceptor)

        return ok.build()
    }

    @Provides
    @Singleton
    fun getRequestInterceptor(
        application: Application,
        context: Context,
        json: Json
    ): Interceptor {
        // Captured by the Interceptor lambda below for decoding error responses.
        // Aliased to `errorJson` to make it explicit at the call site that this
        // is for error-envelope decoding, not for the application's data payloads
        // (those go through Retrofit's converter, set up in provideRestAPI).
        val errorJson = json
        return Interceptor {
            Timber.tag("PRETTY_LOGGER")

            val original = it.request()

            Timber.tag("pretty").e("Interceptor.url.host: %s", original.url.host)
            Timber.tag("pretty").e("Interceptor.url.path: %s", original.url)
            val requested = with(original) {
                val builder = newBuilder()

                builder.header("Accept", "application/json")
                builder.header("Accept-Version", "v1")
                builder.header("mobileplatform", "android")
                Timber.d("mobileplatform: android")

                builder.header("versioncode", "${BuildConfig.VERSION_CODE}")
                Timber.d("versioncode: ${BuildConfig.VERSION_CODE}")

                builder.build()
            }

            val response = it.proceed(requested)
            val body = response.body
            var bodyStr = body.string()
            Timber.d("**http-num: ${response.code}")
            Timber.d("**http-body: $body")

            if (!response.isSuccessful) {
                try {
                    when (response.code) {
                        NetworkError.ERROR_SERVICE_BAD_GATEWAY, NetworkError.ERROR_SERVICE_UNAVAILABLE -> {
                            // Implemented UI
                        }

                        NetworkError.ERROR_SERVICE_UNPROCESSABLE_ENTITY -> {
                            // Decode the error envelope using kotlinx.serialization.
                            // Use a fresh Json instance configured with the same lenient
                            // settings as provideJson() — passing the injected one would
                            // require restructuring this lambda to receive it, which is
                            // out of scope for the migration.
                            val networkError = errorJson.decodeFromString(
                                NetworkError.serializer(),
                                bodyStr
                            )

                            networkError.isNull({

                            }, {
                                networkError.detail[0].msg =
                                    original.url.encodedPath + "\n" + networkError.detail[0].msg
                                bodyStr = errorJson.encodeToString(
                                    NetworkError.serializer(),
                                    networkError
                                )
                            })
                        }

                        else -> {
                            Timber.d("Else What")
                        }
                    }
                } catch (e: Exception) {
                    e.stackTrace
                    Timber.e(Throwable(e.toString()))
                }
            }

            val builder = response.newBuilder()

            builder.body(bodyStr.toByteArray().toResponseBody(body.contentType())).build()
        }
    }

    /**
     * Builds the Retrofit instance that drives [RestAPI].
     *
     * ### Converter chain (order matters)
     *
     * 1. `NullOnEmptyConverterFactory` — handles HTTP 200 with empty body
     *    (Unsplash returns this for some "no content" success cases). Must
     *    be first so it gets a chance to short-circuit before the JSON
     *    converter tries to parse an empty string and throws.
     *
     * 2. `Json.asConverterFactory(...)` — the kotlinx.serialization converter,
     *    keyed off the `application/json` media type. Replaces the previous
     *    `GsonConverterFactory.create(gson)` as part of the Gson →
     *    kotlinx.serialization migration.
     *
     * The `Json` instance is the one provided by [provideJson], so all of its
     * production hardening (`ignoreUnknownKeys`, `coerceInputValues`, etc.)
     * is in effect for every API response.
     */
    @Singleton
    @Provides
    fun provideRestAPI(json: Json, okHttpClient: OkHttpClient): RestAPI {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.apiServer)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(okHttpClient)
            .build()

        return retrofit.create(RestAPI::class.java)
    }
}