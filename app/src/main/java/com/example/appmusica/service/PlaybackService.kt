package com.example.appmusica.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

import androidx.media3.session.MediaNotification
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.common.Player
import androidx.core.app.NotificationCompat
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.data.local.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.google.common.collect.ImmutableList

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var authManager: AuthManager

    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mapOf(
                "ngrok-skip-browser-warning" to "true",
                "Authorization" to "Bearer ${authManager.getToken() ?: ""}"
            ))
            
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory))
            .build()
            
        mediaSession = MediaSession.Builder(this, player).build()

        setMediaNotificationProvider(CustomNotificationProvider(this, authManager))
    }

    private inner class CustomNotificationProvider(
        private val context: Context,
        private val authManager: AuthManager
    ) : MediaNotification.Provider {

        private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        private val channelId = "playback_channel"

        init {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Reproducción", NotificationManager.IMPORTANCE_LOW)
                notificationManager.createNotificationChannel(channel)
            }
        }

        override fun getNotification(
            mediaSession: androidx.media3.session.MediaSession,
            customLayout: com.google.common.collect.ImmutableList<androidx.media3.session.CommandButton>,
            actionFactory: androidx.media3.session.MediaNotification.ActionFactory,
            onNotificationChangedCallback: androidx.media3.session.MediaNotification.Provider.Callback
        ): androidx.media3.session.MediaNotification {
            val player = mediaSession.player
            val metadata = player.mediaMetadata
            
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification_music_vector)
                .setContentTitle(metadata.title ?: "Spotifake")
                .setContentText(metadata.artist ?: "Desconocido")
                .setOngoing(player.isPlaying)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)

            // Acciones básicas
            builder.addAction(actionFactory.createMediaAction(mediaSession, androidx.media3.ui.R.drawable.exo_ic_skip_previous, "Prev", Player.COMMAND_SKIP_TO_PREVIOUS))
            
            val playPauseIcon = if (player.isPlaying) androidx.media3.ui.R.drawable.exo_ic_pause_circle_filled else androidx.media3.ui.R.drawable.exo_ic_play_circle_filled
            builder.addAction(actionFactory.createMediaAction(mediaSession, playPauseIcon, "Play/Pause", Player.COMMAND_PLAY_PAUSE))
            
            builder.addAction(actionFactory.createMediaAction(mediaSession, androidx.media3.ui.R.drawable.exo_ic_skip_next, "Next", Player.COMMAND_SKIP_TO_NEXT))

            builder.setStyle(androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(mediaSession)
                .setShowActionsInCompactView(0, 1, 2))

            // Cargar Arte en segundo plano
            metadata.artworkUri?.let { uri ->
                val glideUrl = GlideUrl(uri.toString(), LazyHeaders.Builder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .addHeader("Authorization", "Bearer ${authManager.getToken() ?: ""}")
                    .build())

                Glide.with(context)
                    .asBitmap()
                    .load(glideUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            builder.setLargeIcon(resource)
                            onNotificationChangedCallback.onNotificationChanged(MediaNotification(1001, builder.build()))
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }

            return androidx.media3.session.MediaNotification(1001, builder.build())
        }

        override fun handleCustomCommand(
            mediaSession: androidx.media3.session.MediaSession,
            actionFactory: androidx.media3.session.MediaNotification.ActionFactory,
            customCommand: String,
            extras: android.os.Bundle
        ): Boolean = false
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
