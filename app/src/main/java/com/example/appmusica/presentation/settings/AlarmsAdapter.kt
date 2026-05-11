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
    private val onEdit: (Alarma) -> Unit,
    private val onDelete: (Alarma) -> Unit,
    private val getSong: (Int) -> com.example.appmusica.domain.model.Cancion?
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
            val song = getSong(alarm.cancionId)
            
            binding.txtTime.text = alarm.hora
            binding.txtSongName.text = song?.nombre ?: "Canción desconocida"
            binding.txtArtistName.text = song?.artista ?: "Artista desconocido"
            
            // Format days
            val dayNames = listOf("L", "M", "X", "J", "V", "S", "D")
            val selectedDays = alarm.dias?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            
            if (selectedDays.isEmpty()) {
                binding.txtDays.text = "Una vez"
            } else {
                val displayText = dayNames.mapIndexed { index, name ->
                    if (selectedDays.contains(index + 1)) name else "-"
                }.joinToString(" ")
                binding.txtDays.text = displayText
            }
            binding.switchActive.setOnCheckedChangeListener(null) // Prevent loop
            binding.switchActive.isChecked = alarm.activo
            
            // Dynamic tinting
            val color = if (alarm.activo) android.graphics.Color.parseColor("#1DB954") else android.graphics.Color.GRAY
            binding.switchActive.thumbTintList = android.content.res.ColorStateList.valueOf(color)
            binding.switchActive.trackTintList = android.content.res.ColorStateList.valueOf(color).withAlpha(128)

            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm, isChecked)
                // Update tint immediately
                val newColor = if (isChecked) android.graphics.Color.parseColor("#1DB954") else android.graphics.Color.GRAY
                binding.switchActive.thumbTintList = android.content.res.ColorStateList.valueOf(newColor)
                binding.switchActive.trackTintList = android.content.res.ColorStateList.valueOf(newColor).withAlpha(128)
            }
            
            binding.btnEdit.setOnClickListener { onEdit(alarm) }
            binding.btnDelete.setOnClickListener { onDelete(alarm) }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<Alarma>() {
        override fun areItemsTheSame(oldItem: Alarma, newItem: Alarma): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Alarma, newItem: Alarma): Boolean = oldItem == newItem
    }
}
