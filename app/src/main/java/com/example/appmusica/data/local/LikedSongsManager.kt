package com.example.appmusica.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages which songs the current user has already liked.
 * Stored in SharedPreferences keyed by userId so each user has independent likes.
 */
@Singleton
class LikedSongsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager
) {
    private fun prefs() = context.getSharedPreferences(
        "liked_songs_${authManager.getUserId()}",
        Context.MODE_PRIVATE
    )

    fun isLiked(cancionId: Int): Boolean =
        prefs().getBoolean("song_$cancionId", false)

    fun setLiked(cancionId: Int) {
        prefs().edit().putBoolean("song_$cancionId", true).apply()
    }

    fun removeLike(cancionId: Int) {
        prefs().edit().putBoolean("song_$cancionId", false).apply()
    }

    /** Flips the like state and returns the NEW state (true = now liked, false = now unliked). */
    fun toggleLike(cancionId: Int): Boolean {
        val newState = !isLiked(cancionId)
        prefs().edit().putBoolean("song_$cancionId", newState).apply()
        return newState
    }
}
