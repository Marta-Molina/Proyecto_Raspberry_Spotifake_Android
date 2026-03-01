package com.example.appmusica.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import com.example.appmusica.R

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mapOf("ngrok-skip-browser-warning" to "true"))
            
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory))
            .build()
            
        mediaSession = MediaSession.Builder(this, player).build()

        // Configurar el icono pequeño de la notificación (vectorial) para la barra de estado
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .build()
        notificationProvider.setSmallIcon(R.drawable.ic_notification_music_vector)
        setMediaNotificationProvider(notificationProvider)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
