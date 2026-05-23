package com.example.appmusica.util

import android.content.Context
import android.content.SharedPreferences
import com.example.appmusica.R

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PALETTE_KEY = "selected_palette"
        private const val DARK_MODE_KEY = "is_dark_mode"

        const val PALETTE_SPOTIFY = "spotify"
        const val PALETTE_GOLD = "gold"
        const val PALETTE_PINK = "pink"
        const val PALETTE_BLUE = "blue"
        const val PALETTE_PURPLE = "purple"

        // For backwards compatibility or simplified logic if needed
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_GOLD = "gold"
        const val THEME_PINK = "pink"
        const val THEME_BLUE = "blue"
        const val THEME_EMERALD = "emerald"
    }

    fun setPalette(palette: String) {
        prefs.edit().putString(PALETTE_KEY, palette).apply()
    }

    fun getPalette(): String {
        val oldTheme = prefs.getString("selected_theme", null)
        if (oldTheme != null) {
            // Migrate old theme to palette
            val palette = when (oldTheme) {
                THEME_GOLD -> PALETTE_GOLD
                THEME_PINK -> PALETTE_PINK
                THEME_BLUE -> PALETTE_BLUE
                THEME_EMERALD -> PALETTE_PURPLE
                else -> PALETTE_SPOTIFY
            }
            setPalette(palette)
            prefs.edit().remove("selected_theme").apply()
            return palette
        }
        return prefs.getString(PALETTE_KEY, PALETTE_SPOTIFY) ?: PALETTE_SPOTIFY
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(DARK_MODE_KEY, isDark).apply()
    }

    fun isDarkMode(): Boolean {
        // If it was THEME_LIGHT, migrate to dark mode false
        if (prefs.contains("selected_theme")) {
            val oldTheme = prefs.getString("selected_theme", "")
            if (oldTheme == THEME_LIGHT) {
                setDarkMode(false)
            }
        }
        return prefs.getBoolean(DARK_MODE_KEY, true)
    }

    fun getTheme(): String {
        return when (getPalette()) {
            PALETTE_GOLD -> THEME_GOLD
            PALETTE_PINK -> THEME_PINK
            PALETTE_BLUE -> THEME_BLUE
            PALETTE_PURPLE -> THEME_EMERALD
            else -> if (isDarkMode()) THEME_DARK else THEME_LIGHT
        }
    }

    fun getThemeResId(): Int {
        val isDark = isDarkMode()
        return when (getPalette()) {
            PALETTE_GOLD -> if (isDark) R.style.Theme_AppMusica_Premium_Gold else R.style.Theme_AppMusica_Premium_Gold_Light
            PALETTE_PINK -> if (isDark) R.style.Theme_AppMusica_Premium_Pink else R.style.Theme_AppMusica_Premium_Pink_Light
            PALETTE_BLUE -> if (isDark) R.style.Theme_AppMusica_Premium_Blue else R.style.Theme_AppMusica_Premium_Blue_Light
            PALETTE_PURPLE -> if (isDark) R.style.Theme_AppMusica_Premium_Purple else R.style.Theme_AppMusica_Premium_Purple_Light
            else -> if (isDark) R.style.Theme_AppMusica else R.style.Theme_AppMusica_Light
        }
    }

    fun getAvailablePalettes(isPremium: Boolean): List<String> {
        val base = listOf(PALETTE_SPOTIFY)
        return if (isPremium) {
            base + listOf(PALETTE_GOLD, PALETTE_PINK, PALETTE_BLUE, PALETTE_PURPLE)
        } else {
            base
        }
    }

    // Keep for compatibility during transition
    fun setTheme(theme: String) {
        when (theme) {
            THEME_DARK -> { setPalette(PALETTE_SPOTIFY); setDarkMode(true) }
            THEME_LIGHT -> { setPalette(PALETTE_SPOTIFY); setDarkMode(false) }
            THEME_GOLD -> setPalette(PALETTE_GOLD)
            THEME_PINK -> setPalette(PALETTE_PINK)
            THEME_BLUE -> setPalette(PALETTE_BLUE)
            THEME_EMERALD -> setPalette(PALETTE_PURPLE)
        }
    }
}
