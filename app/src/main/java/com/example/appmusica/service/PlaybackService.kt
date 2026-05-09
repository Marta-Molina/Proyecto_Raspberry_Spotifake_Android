package com.example.appmusica.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.common.util.BitmapLoader
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.data.local.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.net.Uri

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

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setBitmapLoader(GlideBitmapLoader(this, authManager))
                .build().apply {
                    setSmallIcon(R.drawable.ic_notification_music_vector)
                }
        )
    }

    private class GlideBitmapLoader(
        private val context: Context,
        private val authManager: AuthManager
    ) : BitmapLoader {
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
