package com.example.appmusica.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.appmusica.R
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

class ConfettiManager(private val context: Context) {

    private val themeManager = ThemeManager(context)

    fun getPartyForCurrentTheme(): Party {
        val theme = themeManager.getTheme()

        val colors = when (theme) {
            ThemeManager.THEME_PINK -> listOf(Color.parseColor("#FF69B4"), Color.parseColor("#FFB6C1"), Color.parseColor("#FFC0CB"))
            ThemeManager.THEME_GOLD -> listOf(Color.parseColor("#FFD700"), Color.parseColor("#FFFACD"), Color.parseColor("#FFEC8B"))
            ThemeManager.THEME_BLUE -> listOf(Color.parseColor("#00BFFF"), Color.parseColor("#87CEFA"), Color.parseColor("#B0E2FF"))
            ThemeManager.THEME_EMERALD -> listOf(Color.parseColor("#50C878"), Color.parseColor("#A9DFBF"), Color.parseColor("#2ECC71"))
            else -> listOf(Color.parseColor("#1DB954"), Color.parseColor("#FFE137"), Color.parseColor("#FF5C5C"))
        }

        // Custom shapes based on theme
        val shapes = when (theme) {
            ThemeManager.THEME_PINK -> {
                // Flowers (using a circular shape with varied colors as a simple approximation if no icon)
                listOf(Shape.Circle)
            }
            ThemeManager.THEME_GOLD -> {
                // Stars
                val star = ContextCompat.getDrawable(context, android.R.drawable.btn_star_big_on)
                if (star != null) listOf(Shape.DrawableShape(star, tint = true)) else listOf(Shape.Square)
            }
            ThemeManager.THEME_BLUE -> {
                // Music notes
                val note = ContextCompat.getDrawable(context, R.drawable.ic_nav_music)
                if (note != null) listOf(Shape.DrawableShape(note, tint = true)) else listOf(Shape.Circle)
            }
            ThemeManager.THEME_EMERALD -> {
                // Leaves (using square rotated or similar if no icon)
                listOf(Shape.Square)
            }
            else -> listOf(Shape.Circle, Shape.Square)
        }

        return Party(
            speed = 10f,
            maxSpeed = 35f,
            damping = 0.9f,
            spread = 360,
            colors = colors,
            shapes = shapes,
            size = listOf(Size.SMALL, Size.LARGE),
            timeToLive = 2500L,
            emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.4)
        )
    }
}
