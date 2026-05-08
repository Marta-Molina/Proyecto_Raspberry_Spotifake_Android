package com.example.appmusica.presentation.canciones.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.databinding.ItemLyricLineBinding // reusing a simple line or creating new
import com.example.appmusica.domain.model.Cancion
import android.graphics.Color

class QueueAdapter(
    private var queue: List<Cancion> = emptyList(),
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {

    fun update(newQueue: List<Cancion>) {
        this.queue = newQueue
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLyricLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(queue[position], position)
    }

    override fun getItemCount() = queue.size

    inner class ViewHolder(private val binding: ItemLyricLineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Cancion, position: Int) {
            binding.txtLyricLine.text = "${position + 1}. ${song.nombre}"
            binding.txtLyricLine.setTextColor(Color.WHITE)
            binding.txtLyricLine.alpha = 1.0f
            
            binding.root.setOnClickListener {
                // Optional: play this song from queue
            }
            
            binding.root.setOnLongClickListener {
                onRemove(position)
                true
            }
        }
    }
}
