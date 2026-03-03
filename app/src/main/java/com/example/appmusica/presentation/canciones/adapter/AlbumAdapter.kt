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
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumVH>() {

    inner class AlbumVH(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.txtAlbumName.text = album.nombre
            val url = album.portadaUrl
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
            val fullUrl = if (url?.startsWith("/") == true) "$baseUrl$url" else url

            Glide.with(binding.imgAlbum.context)
                .load(fullUrl)
                .placeholder(R.drawable.portada_generica)
                .error(R.drawable.portada_generica) // Fallback for errors
                .into(binding.imgAlbum)

            binding.root.setOnClickListener { onClick(album.id) }
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
