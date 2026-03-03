package com.example.appmusica.presentation.canciones.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemArtistCardBinding
import com.example.appmusica.domain.model.Artista

class ArtistAdapter(
    private var items: List<Artista>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistVH>() {

    inner class ArtistVH(val binding: ItemArtistCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(artista: Artista) {
            binding.txtArtistName.text = artista.nombre
            
            val url = artista.fotoUrl
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
            val fullUrl = if (url?.startsWith("/") == true) "$baseUrl$url" else url

            if (!fullUrl.isNullOrEmpty()) {
                Glide.with(binding.imgArtist.context)
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgArtist)
            } else {
                binding.imgArtist.setImageResource(R.drawable.placeholder)
            }

            binding.root.setOnClickListener { onClick(artista.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistVH {
        val binding = ItemArtistCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistVH(binding)
    }

    override fun onBindViewHolder(holder: ArtistVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Artista>) {
        items = newItems
        notifyDataSetChanged()
    }
}
