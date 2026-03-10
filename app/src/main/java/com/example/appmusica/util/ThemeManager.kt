package com.example.appmusica.util

import android.content.Context
import android.content.SharedPreferences
import com.example.appmusica.R

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val THEME_KEY = "selected_theme"
        
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_GOLD = "gold"
        const val THEME_PINK = "pink"
        const val THEME_BLUE = "blue"
        const val THEME_EMERALD = "emerald"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(THEME_KEY, theme).apply()
    }

    fun getTheme(): String {
        return prefs.getString(THEME_KEY, THEME_DARK) ?: THEME_DARK
    }

    fun getThemeResId(): Int {
        return when (getTheme()) {
            THEME_LIGHT -> R.style.Theme_AppMusica_Light
            THEME_GOLD -> R.style.Theme_AppMusica_Premium_Gold
            THEME_PINK -> R.style.Theme_AppMusica_Premium_Pink
            THEME_BLUE -> R.style.Theme_AppMusica_Premium_Blue
            THEME_EMERALD -> R.style.Theme_AppMusica_Premium_Emerald
            else -> R.style.Theme_AppMusica // Default Dark
        }
    }
    
    fun getAvailableThemes(isPremium: Boolean): List<String> {
        val base = listOf(THEME_DARK, THEME_LIGHT)
        return if (isPremium) {
            base + listOf(THEME_GOLD, THEME_PINK, THEME_BLUE, THEME_EMERALD)
        } else {
            base
        }
    }
}
