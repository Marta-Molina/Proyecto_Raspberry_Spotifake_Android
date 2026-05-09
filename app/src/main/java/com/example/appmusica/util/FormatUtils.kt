package com.example.appmusica.util

import java.util.Locale

object FormatUtils {
    /**
     * Formatea un número grande a un formato legible con sufijos K y M.
     * Ejemplo: 2700000 -> 2.7M, 1500 -> 1.5K
     */
    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000f)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000f)
            else -> count.toString()
        }
    }
}
