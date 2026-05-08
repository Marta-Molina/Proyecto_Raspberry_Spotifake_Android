package com.example.appmusica.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.databinding.ItemAlarmBinding
import com.example.appmusica.domain.model.Alarma

class AlarmsAdapter(
    private val onToggle: (Alarma, Boolean) -> Unit,
    private val onDelete: (Alarma) -> Unit
) : ListAdapter<Alarma, AlarmsAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: Alarma) {
            binding.txtTime.text = alarm.hora
            binding.txtSongName.text = "Canción ID: ${alarm.cancionId}"
            binding.switchActive.isChecked = alarm.activo
            binding.txtDays.text = alarm.dias ?: "Una vez"

            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm, isChecked)
            }
            
            binding.root.setOnLongClickListener {
                onDelete(alarm)
                true
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<Alarma>() {
        override fun areItemsTheSame(oldItem: Alarma, newItem: Alarma): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Alarma, newItem: Alarma): Boolean = oldItem == newItem
    }
}
