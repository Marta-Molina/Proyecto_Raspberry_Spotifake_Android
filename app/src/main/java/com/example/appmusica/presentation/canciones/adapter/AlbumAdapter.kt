package com.example.appmusica.presentation.canciones.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemAlbumBinding
import com.example.appmusica.domain.model.Album

class AlbumAdapter(
    private var items: List<Album>,
    private val onClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumVH>() {

    inner class AlbumVH(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.txtAlbumName.text = album.nombre
            val url = album.portadaUrl
            if (!url.isNullOrEmpty()) {
                Glide.with(binding.imgAlbum.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgAlbum)
            } else {
                binding.imgAlbum.setImageResource(R.drawable.placeholder)
            }

            binding.root.setOnClickListener { onClick(album) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumVH {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumVH(binding)
    }

    override fun onBindViewHolder(holder: AlbumVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Album>) {
        items = newItems
        notifyDataSetChanged()
    }
}
