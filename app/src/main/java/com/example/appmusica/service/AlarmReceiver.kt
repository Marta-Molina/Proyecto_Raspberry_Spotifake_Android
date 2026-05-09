package com.example.appmusica.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.appmusica.presentation.settings.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val songName = intent.getStringExtra("SONG_NAME") ?: "Alarma"
        val artistName = intent.getStringExtra("ARTIST_NAME") ?: "Spotifake"
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val songUrl = intent.getStringExtra("SONG_URL")

        // Lanzar servicio para el audio y notificación persistente
        // La notificación del servicio se encargará de lanzar la AlarmActivity mediante fullScreenIntent
        val serviceIntent = Intent(context, AlarmNotificationService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("SONG_NAME", songName)
            putExtra("SONG_URL", songUrl)
            putExtra("ARTIST_NAME", artistName)
            putExtra("IMAGE_URL", imageUrl)
            action = "START_ALARM"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
