package com.example.appmusica.presentation.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemPlaylistBinding
import com.example.appmusica.domain.model.Playlist

class PlaylistViewHolder(
    view: View,
    private val onDelete: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onClick: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {
    private val binding = ItemPlaylistBinding.bind(view)

    fun bind(playlist: Playlist) {
        binding.txtviewPlaylistNombre.text = playlist.nombre
        
        binding.btnDeletePlaylist.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onDelete(bindingAdapterPosition)
            }
        }
        
        binding.btnEditPlaylist.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onEdit(bindingAdapterPosition)
            }
        }
        
        itemView.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onClick(bindingAdapterPosition)
            }
        }
    }
}

class PlaylistAdapter(
    private var list: MutableList<Playlist>,
    private val onDelete: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view, onDelete, onEdit, onClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Playlist>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
    
    fun getPlaylist(position: Int): Playlist? = if (position in list.indices) list[position] else null
}
