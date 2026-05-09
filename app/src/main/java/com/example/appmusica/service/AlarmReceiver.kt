package com.example.appmusica.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val songName = intent.getStringExtra("SONG_NAME") ?: "Alarma"
        val artistName = intent.getStringExtra("ARTIST_NAME") ?: "Spotifake"
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val songUrl = intent.getStringExtra("SONG_URL")

        // Lanzar Activity para pantalla completa
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("SONG_NAME", songName)
            putExtra("ARTIST_NAME", artistName)
            putExtra("IMAGE_URL", imageUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(activityIntent)

        // Lanzar servicio para el audio y notificación persistente
        val serviceIntent = Intent(context, AlarmNotificationService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("SONG_NAME", songName)
            putExtra("SONG_URL", songUrl)
            action = "START_ALARM"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
