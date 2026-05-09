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
            
            // Format date from YYYY-MM-DD to DD/MM/YYYY
            val formattedDate = try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(session.date)
                if (date != null) outputFormat.format(date) else session.date
            } catch (e: Exception) {
                session.date
            }

            binding.txtDateTime.text = "$formattedDate ${session.time}"
        }
    }

    class SessionDiffCallback : DiffUtil.ItemCallback<UserSession>() {
        override fun areItemsTheSame(oldItem: UserSession, newItem: UserSession): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserSession, newItem: UserSession): Boolean = oldItem == newItem
    }
}
