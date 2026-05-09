package com.example.appmusica.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.appmusica.R
import com.example.appmusica.presentation.MainActivity
import com.example.appmusica.presentation.settings.AlarmActivity

class AlarmNotificationService : Service() {

    private var player: ExoPlayer? = null
    private val CHANNEL_ID = "alarm_service_channel"
    private val NOTIFICATION_ID = 1002

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setUsage(androidx.media3.common.C.USAGE_ALARM)
            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_SONIFICATION)
            .build()
            
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songName = intent?.getStringExtra("SONG_NAME") ?: "Alarma"
        val songUrl = intent?.getStringExtra("SONG_URL")
        val artistName = intent?.getStringExtra("ARTIST_NAME") ?: "Spotifake"
        val imageUrl = intent?.getStringExtra("IMAGE_URL")

        // Call startForeground ASAP (Android 12+ requirement)
        val initialNotification = createNotification(songName, artistName, imageUrl)
        startForeground(NOTIFICATION_ID, initialNotification)

        if (intent?.action == "STOP_ALARM") {
            player?.stop()
            player?.release()
            player = null
            
            // Notify activity to finish
            sendBroadcast(Intent("ACTION_STOP_ALARM_ACTIVITY"))
            
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        playSong(songUrl)
        return START_STICKY
    }

    private fun playSong(songUrl: String?) {
        if (!songUrl.isNullOrEmpty()) {
            try {
                val fullUrl = if (songUrl.startsWith("http")) songUrl else {
                    com.example.appmusica.di.NetworkModule.BASE_STATIC_URL.removeSuffix("/") + "/" + songUrl.removePrefix("/")
                }
                val mediaItem = MediaItem.fromUri(fullUrl)
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotification(songName: String, artistName: String, imageUrl: String?): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alarmas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notificaciones de alarma sonando"
                setSound(null, null) 
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, AlarmNotificationService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val fullScreenIntent = Intent(this, com.example.appmusica.presentation.settings.AlarmActivity::class.java).apply {
            putExtra("SONG_NAME", songName)
            putExtra("ARTIST_NAME", artistName)
            putExtra("IMAGE_URL", imageUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("¡Alarma Spotifake!")
            .setContentText("Sonando: $songName")
            .setSmallIcon(com.example.appmusica.R.drawable.spotify_black_circle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(com.example.appmusica.R.drawable.ic_close, "Detener", stopPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        player?.stop()
        player?.release()
        player = null
        super.onDestroy()
    }
}
