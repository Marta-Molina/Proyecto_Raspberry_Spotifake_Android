package com.example.appmusica.util

import android.view.MotionEvent
import android.view.View

/**
 * Agrega una animación de escala al tocar la vista.
 * Simula el comportamiento de un botón premium.
 */
fun View.setClickAnimation() {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
        }
        false // Importante: retornar false para no consumir el evento de click
    }
}
