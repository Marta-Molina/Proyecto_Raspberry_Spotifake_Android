package com.example.appmusica.presentation.canciones.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.databinding.ItemLyricLineBinding
import com.example.appmusica.domain.model.LetraSync
import android.graphics.Color
import android.graphics.Typeface

class LyricsAdapter(private var lyrics: List<LetraSync> = emptyList()) : RecyclerView.Adapter<LyricsAdapter.ViewHolder>() {
    private var activeLineIndex = -1

    fun updateLyrics(newLyrics: List<LetraSync>) {
        this.lyrics = newLyrics
        notifyDataSetChanged()
    }

    fun getActiveIndex() = activeLineIndex

    fun setActiveLine(currentTimeMs: Long): Int {
        val index = lyrics.indexOfLast { it.timestamp.toLong() <= currentTimeMs }
        if (index != activeLineIndex && index != -1) {
            val old = activeLineIndex
            activeLineIndex = index
            notifyItemChanged(old)
            notifyItemChanged(activeLineIndex)
            return activeLineIndex
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLyricLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lyrics[position], position == activeLineIndex)
    }

    override fun getItemCount() = lyrics.size

    inner class ViewHolder(private val binding: ItemLyricLineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(line: LetraSync, isActive: Boolean) {
            binding.txtLyricLine.text = line.texto
            binding.txtLyricLine.alpha = if (isActive) 1f else 0.4f
            binding.txtLyricLine.scaleX = if (isActive) 1.1f else 1.0f
            binding.txtLyricLine.scaleY = if (isActive) 1.1f else 1.0f
            binding.txtLyricLine.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
            binding.txtLyricLine.setTextColor(if (isActive) Color.parseColor("#1DB954") else Color.WHITE)
        }
    }
}
