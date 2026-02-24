package com.example.appmusica.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "jwt_token"
        private const val USER_ID_KEY = "user_id"
        private const val IS_ADMIN_KEY = "is_admin"
        private const val URL_IMAGEN_KEY = "url_imagen"
    }

    // Reactive stream so any observer can react to profile picture changes immediately
    private val _profileImageUrl = MutableStateFlow(getUrlImagen())
    val profileImageUrl: StateFlow<String?> = _profileImageUrl

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun saveUserId(id: Long) {
        prefs.edit().putLong(USER_ID_KEY, id).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(USER_ID_KEY, -1L)
    }

    fun saveIsAdmin(isAdmin: Boolean) {
        prefs.edit().putBoolean(IS_ADMIN_KEY, isAdmin).apply()
    }

    fun isAdmin(): Boolean {
        return prefs.getBoolean(IS_ADMIN_KEY, false)
    }

    fun saveUrlImagen(url: String?) {
        prefs.edit().putString(URL_IMAGEN_KEY, url).apply()
        _profileImageUrl.value = url
    }

    fun getUrlImagen(): String? {
        return prefs.getString(URL_IMAGEN_KEY, null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
