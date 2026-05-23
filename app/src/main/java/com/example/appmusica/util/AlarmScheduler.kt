package com.example.appmusica.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.appmusica.domain.model.Alarma
import com.example.appmusica.service.AlarmReceiver
import java.util.*

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarma, songUrl: String?, artistName: String? = null, imageUrl: String? = null) {
        if (!alarm.activo) {
            cancel(alarm.id)
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("SONG_NAME", alarm.nombre)
            putExtra("ARTIST_NAME", artistName)
            putExtra("IMAGE_URL", imageUrl)
            putExtra("SONG_URL", songUrl)
            putExtra("ALARM_DIAS", alarm.dias)
            putExtra("ALARM_HORA", alarm.hora)
            putExtra("CANCION_ID", alarm.cancionId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = alarm.hora.split(":")
        if (timeParts.size != 2) return

        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val selectedDays = alarm.dias?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() } ?: emptyList()

        if (selectedDays.isEmpty()) {
            // One-time alarm
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Repeating alarm - find the next valid day starting from today
            var found = false
            for (i in 0..7) {
                val checkCalendar = calendar.clone() as Calendar
                checkCalendar.add(Calendar.DAY_OF_YEAR, i)

                // Convert current check day to 1-7 (Mon-Sun)
                val dayOfWeek = when(checkCalendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }

                if (selectedDays.contains(dayOfWeek)) {
                    if (i == 0) {
                        // It's today, check if time has already passed
                        if (checkCalendar.timeInMillis > System.currentTimeMillis()) {
                            calendar.timeInMillis = checkCalendar.timeInMillis
                            found = true
                            break
                        }
                    } else {
                        calendar.timeInMillis = checkCalendar.timeInMillis
                        found = true
                        break
                    }
                }
            }

            if (!found) {
                // Fallback (should not happen with i in 0..7)
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // Show notification with unique ID per alarm to avoid overwriting
        updateUpcomingAlarmNotification(alarm.id, alarm.hora)
    }

    fun snooze(alarmId: Int, songName: String?, songUrl: String?, artistName: String?, imageUrl: String?) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 5)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("SONG_NAME", songName)
            putExtra("SONG_URL", songUrl)
            putExtra("ARTIST_NAME", artistName)
            putExtra("IMAGE_URL", imageUrl)
            putExtra("IS_SNOOZE", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancel(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
        hideUpcomingAlarmNotification(alarmId)
    }

    private fun updateUpcomingAlarmNotification(id: Int, time: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val CHANNEL_ID = "scheduled_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(CHANNEL_ID, "Alarmas Programadas", android.app.NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.appmusica.R.drawable.ic_alarm)
            .setContentTitle("Alarma programada")
            .setContentText("Próxima alarma a las $time")
            .setOngoing(true)
            .build()

        notificationManager.notify(id + 2000, notification)
    }

    private fun hideUpcomingAlarmNotification(id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (id == -1) {
            // Cancel all alarm notifications if needed
            return
        }
        notificationManager.cancel(id + 2000)
    }
}
