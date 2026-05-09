package com.example.appmusica.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.appmusica.R
import com.example.appmusica.presentation.MainActivity

class AlarmNotificationService : Service() {

    private var player: ExoPlayer? = null
    private val CHANNEL_ID = "alarm_service_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songUrl = intent?.getStringExtra("SONG_URL")
        val songName = intent?.getStringExtra("SONG_NAME") ?: "Alarma Spotifake"
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1

        if (intent?.action == "STOP_ALARM") {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification(songName)
        startForeground(1002, notification)

        if (songUrl != null) {
            playAlarmMusic(songUrl)
        }

        return START_NOT_STICKY
    }

    private fun playAlarmMusic(url: String) {
        player?.release()
        
        val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mapOf("ngrok-skip-browser-warning" to "true"))
            
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory))
            .build().apply {
            val fullUrl = if (url.startsWith("http")) url else {
                com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/") + url
            }
            setMediaItem(MediaItem.fromUri(fullUrl))
            prepare()
            play()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    private fun createNotification(songName: String): Notification {
        val stopIntent = Intent(this, AlarmNotificationService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("¡Alarma Spotifake!")
            .setContentText("Sonando: $songName")
            .setSmallIcon(R.drawable.ic_notification_music_vector)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Alarma",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para las alarmas de Spotifake"
                setSound(null, null) // Usamos ExoPlayer para el sonido
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        player?.stop()
        player?.release()
        player = null
        super.onDestroy()
    }
}
