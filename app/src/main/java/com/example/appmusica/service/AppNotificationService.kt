package com.example.appmusica.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.example.appmusica.R
import com.example.appmusica.domain.repository.SocialRepository
import com.example.appmusica.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AppNotificationService : Service() {

    @Inject
    lateinit var socialRepository: SocialRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "app_notifications_channel"
        private const val NOTIFICATION_ID = 888
        private const val POLL_INTERVAL = 30000L // 30 seconds

        fun start(context: Context) {
            val intent = Intent(context, AppNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createPersistentNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, createPersistentNotification())
        }
        startPolling()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Spotifake"
            val descriptionText = "Notificaciones de amigos, listas compartidas y nuevos lanzamientos"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPersistentNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Spotifake está activo")
            .setContentText("Buscando nuevas notificaciones...")
            .setSmallIcon(R.drawable.ic_nav_music)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = serviceScope.launch {
            while (isActive) {
                try {
                    val notifications = socialRepository.getNotifications()
                    val unread = notifications.filter { !it.leida }

                    unread.forEach { notification ->
                        showSystemNotification(notification)
                        socialRepository.markNotificationAsRead(notification.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(POLL_INTERVAL)
            }
        }
    }

    private fun showSystemNotification(notification: com.example.appmusica.domain.model.Notificacion) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, notification.id, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_music)
            .setContentTitle(notification.titulo)
            .setContentText(notification.mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.id, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        pollJob?.cancel()
        serviceScope.cancel()
    }
}
