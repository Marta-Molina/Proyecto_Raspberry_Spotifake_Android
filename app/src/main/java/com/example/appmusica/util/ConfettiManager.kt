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
        val palette = themeManager.getPalette()

        val colors = when (palette) {
            ThemeManager.PALETTE_PINK -> listOf(Color.parseColor("#FF69B4"), Color.parseColor("#FFB6C1"), Color.parseColor("#FFC0CB"))
            ThemeManager.PALETTE_GOLD -> listOf(Color.parseColor("#FFD700"), Color.parseColor("#FFFACD"), Color.parseColor("#FFEC8B"))
            ThemeManager.PALETTE_BLUE -> listOf(Color.parseColor("#00BFFF"), Color.parseColor("#87CEFA"), Color.parseColor("#B0E2FF"))
            ThemeManager.PALETTE_PURPLE -> listOf(Color.parseColor("#9B59B6"), Color.parseColor("#D2B4DE"), Color.parseColor("#AF7AC5"))
            else -> listOf(Color.parseColor("#1DB954"), Color.parseColor("#FFE137"), Color.parseColor("#FF5C5C"))
        }

        // Custom shapes based on palette
        val shapes = when (palette) {
            ThemeManager.PALETTE_PINK -> {
                listOf(Shape.Circle)
            }
            ThemeManager.PALETTE_GOLD -> {
                val star = ContextCompat.getDrawable(context, android.R.drawable.btn_star_big_on)
                if (star != null) listOf(Shape.DrawableShape(star, tint = true)) else listOf(Shape.Square)
            }
            ThemeManager.PALETTE_BLUE -> {
                val note = ContextCompat.getDrawable(context, R.drawable.ic_nav_music)
                if (note != null) listOf(Shape.DrawableShape(note, tint = true)) else listOf(Shape.Circle)
            }
            ThemeManager.PALETTE_PURPLE -> {
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
