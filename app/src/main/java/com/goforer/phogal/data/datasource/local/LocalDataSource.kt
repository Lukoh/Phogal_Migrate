package com.goforer.phogal.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phogal_preferences")

@Singleton
class LocalDataSource @Inject constructor(
    private val context: Context,
    private val json: Json,
    private val cookieJar: PersistentCookieJar? = null
) {
    private object PreferencesKeys {
        val BOOKMARK_PHOTOS = stringPreferencesKey("key_bookmark_photos")
        val SEARCH_WORDS = stringPreferencesKey("search_word_list")
        val FOLLOWING_USER = stringPreferencesKey("key_following_user")
        val NOTIF_FOLLOWING = booleanPreferencesKey("key_notification_following_enabled")
        val NOTIF_LATEST = booleanPreferencesKey("key_notification_latest_enabled")
        val NOTIF_COMMUNITY = booleanPreferencesKey("key_notification_community_enabled")
    }

    private val pictureListSerializer = ListSerializer(Picture.serializer())
    private val userListSerializer = ListSerializer(User.serializer())
    private val stringListSerializer = ListSerializer(String.serializer())

    private fun Flow<Preferences>.handleErrors(): Flow<Preferences> = this.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    suspend fun logOut() {
        Timber.e("LocalDataSource - Log out")
        clearPreference()
        deleteCache(context)
        cookieJar?.clear()
    }

    private fun deleteCache(context: Context) {
        runCatching {
            deleteDir(context.cacheDir)
        }.onFailure { e -> Timber.e(e, "Failed to delete cache") }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false
            for (child in children) {
                if (!deleteDir(File(dir, child))) return false
            }
            dir.delete()
        } else {
            dir?.delete() ?: false
        }
    }

    suspend fun clearPreference() {
        Timber.d("LocalDataSource - Clear preference")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val bookmarkedPhotosFlow: Flow<List<Picture>> = context.dataStore.data
        .map { preferences ->
            val jsonStr = preferences[PreferencesKeys.BOOKMARK_PHOTOS]
            if (jsonStr.isNullOrEmpty()) {
                emptyList()
            } else {
                runCatching { json.decodeFromString(pictureListSerializer, jsonStr) }
                    .getOrElse {
                        Timber.w(it, "Failed to parse stored bookmarks")
                        emptyList()
                    }
            }
        }

    fun isPhotoBookmarkedFlow(id: String): Flow<Boolean> = bookmarkedPhotosFlow
        .map { photos ->
            photos.any { it.id == id }
        }

    fun isPhotoBookmarkedFlow(photo: Picture): Flow<Boolean> = bookmarkedPhotosFlow
        .map { photos ->
            photos.any { it.id == photo.id || it.urls.raw == photo.urls.raw }
        }

    suspend fun toggleBookmarkPhoto(bookmarkedPhoto: Picture) {
        context.dataStore.edit { preferences ->
            val jsonStr = preferences[PreferencesKeys.BOOKMARK_PHOTOS]
            val photos = if (jsonStr.isNullOrEmpty()) {
                mutableListOf()
            } else {
                runCatching { json.decodeFromString(pictureListSerializer, jsonStr).toMutableList() }
                    .getOrDefault(mutableListOf())
            }

            val existingPhoto = photos.find { it.id == bookmarkedPhoto.id }
            if (existingPhoto == null) {
                photos.add(bookmarkedPhoto)
            } else {
                photos.remove(existingPhoto)
            }

            preferences[PreferencesKeys.BOOKMARK_PHOTOS] = json.encodeToString(pictureListSerializer, photos)
        }
    }

    val searchWordsFlow: Flow<List<String>> = context.dataStore.data
        .handleErrors()
        .map { preferences ->
            val jsonStr = preferences[PreferencesKeys.SEARCH_WORDS]
            if (jsonStr.isNullOrEmpty()) emptyList()
            else runCatching { json.decodeFromString(stringListSerializer, jsonStr) }.getOrDefault(emptyList())
        }

    suspend fun setSearchWords(words: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_WORDS] = json.encodeToString(stringListSerializer, words)
        }
    }

    val followingUsersFlow: Flow<List<User>> = context.dataStore.data
        .handleErrors()
        .map { preferences ->
            val jsonStr = preferences[PreferencesKeys.FOLLOWING_USER]
            if (jsonStr.isNullOrEmpty()) emptyList()
            else runCatching { json.decodeFromString(userListSerializer, jsonStr) }.getOrDefault(emptyList())
        }

    fun isUserFollowedFlow(user: User): Flow<Boolean> = followingUsersFlow
        .map { users ->
            users.any { it.id == user.id || it.username == user.username }
        }

    suspend fun toggleFollowingUser(user: User) {
        context.dataStore.edit { preferences ->
            val jsonStr = preferences[PreferencesKeys.FOLLOWING_USER]
            val users = if (jsonStr.isNullOrEmpty()) mutableListOf()
            else runCatching { json.decodeFromString(userListSerializer, jsonStr).toMutableList() }.getOrDefault(mutableListOf())

            val existingUser = users.find { it.id == user.id }
            if (existingUser == null) users.add(user) else users.remove(existingUser)

            preferences[PreferencesKeys.FOLLOWING_USER] = json.encodeToString(userListSerializer, users)
        }
    }

    val enabledFollowingNotificationFlow: Flow<Boolean> = context.dataStore.data
        .handleErrors()
        .map { it[PreferencesKeys.NOTIF_FOLLOWING] ?: true }

    suspend fun setFollowingNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIF_FOLLOWING] = enabled }
    }

    val enabledLatestNotificationFlow: Flow<Boolean> = context.dataStore.data
        .handleErrors()
        .map { it[PreferencesKeys.NOTIF_LATEST] ?: true }

    suspend fun setLatestNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIF_LATEST] = enabled }
    }

    val enableCommunityNotificationFlow: Flow<Boolean> = context.dataStore.data
        .handleErrors()
        .map { it[PreferencesKeys.NOTIF_COMMUNITY] ?: true }

    suspend fun setCommunityNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIF_COMMUNITY] = enabled }
    }
}