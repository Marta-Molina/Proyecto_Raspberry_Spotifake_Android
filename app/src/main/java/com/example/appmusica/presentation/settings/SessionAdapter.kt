package com.example.appmusica.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.data.local.entities.UserSession
import com.example.appmusica.databinding.ItemSessionBinding

class SessionAdapter : ListAdapter<UserSession, SessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(private val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: UserSession) {
            binding.txtAction.text = session.action
            binding.txtDateTime.text = "${session.date} ${session.time}"
            // NO mostrar el token en la UI por razones de seguridad. El token sigue almacenado en Room.
        }
    }

    class SessionDiffCallback : DiffUtil.ItemCallback<UserSession>() {
        override fun areItemsTheSame(oldItem: UserSession, newItem: UserSession): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserSession, newItem: UserSession): Boolean = oldItem == newItem
    }
}
