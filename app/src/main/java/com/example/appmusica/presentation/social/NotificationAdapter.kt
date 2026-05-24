package com.example.appmusica.presentation.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemNotificationBinding
import com.example.appmusica.domain.model.Notificacion
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationAdapter(
    private val onClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private var notifications: List<Notificacion> = emptyList()

    fun update(newList: List<Notificacion>) {
        notifications = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(notifications[position])
    override fun getItemCount() = notifications.size

    inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notif: Notificacion) {
            binding.txtNotifTitle.text = notif.titulo
            binding.txtNotifMessage.text = notif.mensaje

            // Format date/time
            try {
                val dateTime = LocalDateTime.parse(notif.fecha)
                binding.txtNotifDate.text = formatRelativeTime(dateTime)
            } catch (e: Exception) {
                binding.txtNotifDate.text = notif.fecha
            }

            // Set icon based on type
            val iconRes = when (notif.tipo) {
                "friend_request" -> R.drawable.ic_nav_social
                "friend_accepted" -> R.drawable.ic_nav_social
                "playlist_shared" -> R.drawable.ic_nav_playlist
                "new_release" -> R.drawable.ic_nav_music
                else -> R.drawable.ic_nav_music
            }
            binding.ivNotifIcon.setImageResource(iconRes)

            // Visual feedback for unread
            if (!notif.leida) {
                binding.root.alpha = 1.0f
                binding.txtNotifTitle.setTextColor(binding.root.context.getColor(R.color.spotify_green))
            } else {
                binding.root.alpha = 0.7f
                binding.txtNotifTitle.setTextColor(binding.root.context.getColor(R.color.white))
            }

            binding.root.setOnClickListener { onClick(notif) }
        }

        private fun formatRelativeTime(dateTime: LocalDateTime): String {
            val now = LocalDateTime.now()
            val minutes = ChronoUnit.MINUTES.between(dateTime, now)
            val hours = ChronoUnit.HOURS.between(dateTime, now)
            val days = ChronoUnit.DAYS.between(dateTime, now)

            return when {
                minutes < 1 -> "Ahora mismo"
                minutes < 60 -> "Hace $minutes min"
                hours < 24 -> "Hace $hours h"
                days < 7 -> "Hace $days d"
                else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        }
    }
}
