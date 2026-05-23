package com.example.appmusica.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.common.Player
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaItem

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.data.local.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.common.collect.ImmutableList
import androidx.media3.common.util.BitmapLoader
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.CommandButton

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
            
        val intent = Intent(this, com.example.appmusica.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(CustomMediaSessionCallback())
            .setBitmapLoader(GlideBitmapLoader(this, authManager))
            .build()

        setMediaNotificationProvider(
            CustomNotificationProvider(this)
        )
    }

    @OptIn(UnstableApi::class)
    private inner class CustomNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {
        
        init {
            setSmallIcon(R.drawable.ic_notification_music_vector)
        }

        override fun getMediaButtons(
            session: MediaSession,
            playerCommands: Player.Commands,
            customLayout: ImmutableList<CommandButton>,
            showWhenCollapsed: Boolean
        ): ImmutableList<CommandButton> {
            val buttons = ImmutableList.builder<CommandButton>()
            
            // Previous
            buttons.add(CommandButton.Builder()
                .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
                .setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_previous)
                .setDisplayName("Anterior")
                .build())
                
            // Play/Pause
            val playPauseIcon = if (session.player.isPlaying) 
                androidx.media3.ui.R.drawable.exo_ic_pause_circle_filled 
            else 
                androidx.media3.ui.R.drawable.exo_ic_play_circle_filled
                
            buttons.add(CommandButton.Builder()
                .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                .setIconResId(playPauseIcon)
                .setDisplayName("Play/Pause")
                .build())
                
            // Next
            buttons.add(CommandButton.Builder()
                .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_next)
                .setDisplayName("Siguiente")
                .build())
                
            return buttons.build()
        }
    }

    private class GlideBitmapLoader(
        private val context: Context,
        private val authManager: AuthManager
    ) : BitmapLoader {

        override fun supportsMimeType(mimeType: String): Boolean = true

        override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
            val future = SettableFuture.create<Bitmap>()
            Glide.with(context)
                .asBitmap()
                .load(data)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        future.set(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        future.setException(RuntimeException("Load cleared"))
                    }
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        future.setException(RuntimeException("Load failed"))
                    }
                })
            return future
        }

        override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
            val future = SettableFuture.create<Bitmap>()
            val glideUrl = GlideUrl(uri.toString(), LazyHeaders.Builder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .addHeader("Authorization", "Bearer ${authManager.getToken() ?: ""}")
                .build())

            Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        future.set(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        future.setException(RuntimeException("Load cleared"))
                    }
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        future.setException(RuntimeException("Load failed"))
                    }
                })
            return future
        }
    }

    private fun stopWithFade() {
        val player = mediaSession?.player ?: return
        val handler = Handler(Looper.getMainLooper())
        var volume = 1f
        handler.post(object : Runnable {
            override fun run() {
                volume -= 0.1f
                if (volume > 0) {
                    player.volume = volume
                    handler.postDelayed(this, 100)
                } else {
                    player.pause()
                    player.volume = 1f 
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_STOP_FADE") {
            stopWithFade()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @OptIn(UnstableApi::class)
    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val playerCommands = connectionResult.availablePlayerCommands.buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .build()
            return MediaSession.ConnectionResult.accept(connectionResult.availableSessionCommands, playerCommands)
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            if (playerCommand == Player.COMMAND_SEEK_TO_NEXT || 
                playerCommand == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ||
                playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS ||
                playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM) {
                
                if (!authManager.canSkip()) {
                    return androidx.media3.session.SessionResult.RESULT_ERROR_PERMISSION_DENIED
                }
                authManager.incrementSkip()
            }
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }
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
