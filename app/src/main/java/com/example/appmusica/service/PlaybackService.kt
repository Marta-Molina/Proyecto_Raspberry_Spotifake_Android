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

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.content.Context
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
            
        mediaSession = MediaSession.Builder(this, player)
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
                .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT)
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
