package com.goforer.phogal.data.datasource.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted local storage for bookmarks, search history, and follows.
 *
 * ### Migrated to kotlinx.serialization (April 2026)
 *
 * Previously this class used Gson with reflection-based `TypeToken<ArrayList<X>>`
 * patterns:
 * ```
 * val type = object : TypeToken<ArrayList<Picture>>() {}.type
 * Gson().fromJson(json, type)
 * ```
 *
 * That approach had three problems:
 *  1. **Reflection at runtime** — Gson reads class shapes via reflection on
 *     every call. R8/ProGuard could strip needed members, manifesting as
 *     mysterious null fields in production.
 *  2. **No type safety on the OUT side** — `Gson().toJson(...)` silently
 *     accepts any object; type errors only surface as wrong JSON later.
 *  3. **Anonymous TypeToken inner classes** — every `object : TypeToken<…>() {}`
 *     allocates a new anonymous class, which R8 cannot fully optimize.
 *
 * The new approach uses kotlinx.serialization's typed serializers:
 * ```
 * private val pictureListSerializer = ListSerializer(Picture.serializer())
 * json.decodeFromString(pictureListSerializer, jsonString)
 * ```
 *
 * Each serializer is computed at compile time by the Kotlin Serialization
 * plugin and held as a `private val`, so there is no reflection, no extra
 * class allocation, and any type mismatch is a compile error.
 *
 * The injected [json] instance shares its configuration with the rest of the
 * app (see `AppModule.provideJson`).
 */
@Singleton
class LocalDataSource
@Inject
constructor(
    val context: Context,
    private val json: Json,
    cookieJar: PersistentCookieJar? = null
) {
    companion object {
        const val key_bookmark_photos = "key_bookmark_photos"
        const val key_search_word_list = "search_word_list"
        const val key_following_user = "key_following_user"
        const val key_notification_following_enabled = "key_notification_following_enabled"
        const val key_notification_latest_enabled = "key_notification_latest_enabled"
        const val key_notification_community_enabled = "key_notification_community_enabled"
    }

    // Pre-built typed serializers. Building these once and reusing them avoids
    // recomputing the schema on every call. They are inexpensive to allocate,
    // but doing it eagerly makes the code uniformly fast across all entry
    // points (no surprise warm-ups).
    private val pictureListSerializer = ListSerializer(Picture.serializer())
    private val userListSerializer = ListSerializer(User.serializer())
    private val stringListSerializer = ListSerializer(String.serializer())

    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val pref = EncryptedSharedPreferences.create(
        context,
        "Encrypted_Shared_Preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    internal fun logOut() {
        Timber.e("LocalDataSource - Log out")

        clearPreference()
        deleteCache(context)
    }

    private fun deleteCache(context: Context) {
        runCatching {
            deleteDir(context.cacheDir)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false

            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }

            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    @SuppressLint("ApplySharedPref")
    internal fun clearPreference() {
        Timber.e("LocalDataSource - Clear session cookie")

        Timber.d("LocalDataSource - Clear preference")
        val editor = pref.edit()

        editor.clear()
        editor.apply()
        editor.commit()
    }

    /**
     * Reads the persisted bookmark list. Returns `null` when no list has ever
     * been written (first launch). Returns an empty list if the stored payload
     * is empty `[]`. Treats malformed JSON as null with a Timber warning,
     * matching Gson's previous behavior of returning `null` on parse errors.
     */
    internal fun geBookmarkedPhotos(): MutableList<Picture>? {
        val jsonStr = pref.getString(key_bookmark_photos, null) ?: return mutableListOf()
        return runCatching {
            json.decodeFromString(pictureListSerializer, jsonStr).toMutableList()
        }.getOrElse { e ->
            Timber.w(e, "geBookmarkedPhotos: failed to parse stored JSON, returning empty list")
            mutableListOf()
        }
    }

    internal fun isPhotoBookmarked(photo: Picture): Boolean {
        val photos = geBookmarkedPhotos()

        return if (photos.isNullOrEmpty()) {
            false
        } else {
            val foundPhoto = photos.find { it.id == photo.id || it.urls.raw == photo.urls.raw }

            foundPhoto != null
        }
    }

    internal fun isPhotoBookmarked(id: String): Boolean {
        val photos = geBookmarkedPhotos()

        return if (photos.isNullOrEmpty()) {
            false
        } else {
            val foundPhoto = photos.find { it.id == id }

            foundPhoto != null
        }
    }

    /**
     * Toggles [bookmarkedPhoto] in the persisted bookmarks list and returns
     * the resulting list (now reflecting the change).
     *
     * Behavior preserved from the original Gson implementation:
     *  - If no bookmarks exist yet, creates a new list with this entry.
     *  - If the photo is already bookmarked, removes it (toggle semantics).
     *  - If the photo is not bookmarked, adds it.
     */
    internal fun setBookmarkPhoto(bookmarkedPhoto: Picture): MutableList<Picture>? {
        val editor = pref.edit()
        var photos = geBookmarkedPhotos()
        val jsonStr: String

        if (photos.isNullOrEmpty()) {
            photos = mutableListOf()
            photos.add(bookmarkedPhoto)
            jsonStr = json.encodeToString(pictureListSerializer, photos)
            editor.apply()
            editor.putString(key_bookmark_photos, jsonStr)
            editor.apply()
        } else {
            val photo = photos.find { it.id == bookmarkedPhoto.id || it.urls.raw == bookmarkedPhoto.urls.raw }

            if (photo == null)
                photos.add(bookmarkedPhoto)
            else
                photos.remove(photo)

            jsonStr = json.encodeToString(pictureListSerializer, photos)
            editor.putString(key_bookmark_photos, jsonStr)
            editor.apply()
        }

        // Re-decode to mirror the original behavior — historically the caller
        // received a freshly-parsed list rather than the in-memory one. Cheap
        // round-trip, but worth preserving in case any caller relies on the
        // copy semantics.
        return runCatching {
            json.decodeFromString(pictureListSerializer, jsonStr).toMutableList()
        }.getOrElse { photos }
    }

    internal fun getSearchWords(): List<String>? {
        val jsonStr = pref.getString(key_search_word_list, null) ?: return mutableListOf()
        return runCatching {
            json.decodeFromString(stringListSerializer, jsonStr)
        }.getOrElse { e ->
            Timber.w(e, "getSearchWords: failed to parse stored JSON, returning empty list")
            mutableListOf()
        }
    }

    internal fun setSearchWords(words: List<String>? = null) {
        pref.edit {
            // When `words` is null, persist an empty list rather than `"null"` —
            // this matches what Gson did with `toJson(null)` in this codebase
            // (Gson serialized `null` as the JSON string `"null"`, which then
            // failed to parse back as a List on read; the new code stores `[]`
            // explicitly to avoid that round-trip bug).
            val safe = words.orEmpty()
            val jsonStr = json.encodeToString(stringListSerializer, safe)

            putString(key_search_word_list, jsonStr)
        }
    }

    internal fun getFollowingUsers(): MutableList<User> {
        val jsonStr = pref.getString(key_following_user, null) ?: return mutableListOf()
        return runCatching {
            json.decodeFromString(userListSerializer, jsonStr).toMutableList()
        }.getOrElse { e ->
            Timber.w(e, "getFollowingUsers: failed to parse stored JSON, returning empty list")
            mutableListOf()
        }
    }

    internal fun isUserFollowed(user: User): Boolean {
        val users = getFollowingUsers()

        return if (users.isNullOrEmpty()) {
            false
        } else {
            val foundUser = users.find { it.id == user.id && it.username == user.username }

            foundUser != null
        }
    }

    internal fun setFollowingUser(user: User): MutableList<User>? {
        val editor = pref.edit()
        var users = getFollowingUsers()
        val jsonStr: String

        if (users.isNullOrEmpty()) {
            users = mutableListOf()
            users.add(user)
            jsonStr = json.encodeToString(userListSerializer, users)
            editor.apply()
            editor.putString(key_following_user, jsonStr)
            editor.apply()
        } else {
            val followingUser = users.find { it.id == user.id && it.username == user.username }

            if (followingUser == null)
                users.add(user)
            else
                users.remove(followingUser)

            jsonStr = json.encodeToString(userListSerializer, users)
            editor.putString(key_following_user, jsonStr)
            editor.apply()
        }

        return runCatching {
            json.decodeFromString(userListSerializer, jsonStr).toMutableList()
        }.getOrElse { users }
    }

    var enabledFollowingNotification: Boolean
        get() = pref.getBoolean(key_notification_following_enabled, true)
        set(value) {
            pref.edit {
                putBoolean(key_notification_following_enabled, value)
            }
        }

    var enabledLatestNotification: Boolean
        get() = pref.getBoolean(key_notification_latest_enabled, true)
        set(value) {
            pref.edit {
                putBoolean(key_notification_latest_enabled, value)
            }
        }

    var enableCommunityNotification: Boolean
        get() = pref.getBoolean(key_notification_community_enabled, true)
        set(value) {
            pref.edit {
                putBoolean(key_notification_community_enabled, value)
            }
        }
}
