package com.example.appmusica.presentation.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.util.setClickAnimation

class ManageSongsAdapter(
    private var songs: List<Cancion>,
    private val onEdit: (Cancion) -> Unit,
    private val onDelete: (Cancion) -> Unit
) : RecyclerView.Adapter<ManageSongsAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSongPortada: ImageView = view.findViewById(R.id.ivSongPortada)
        val tvSongName: TextView = view.findViewById(R.id.tvSongName)
        val tvSongArtist: TextView = view.findViewById(R.id.tvSongArtist)
        val btnEditSong: ImageButton = view.findViewById(R.id.btnEditSong)
        val btnDeleteSong: ImageButton = view.findViewById(R.id.btnDeleteSong)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.tvSongName.text = song.nombre
        holder.tvSongArtist.text = song.artista

        val baseUrl = NetworkModule.BASE_URL.replace("/api/", "").removeSuffix("/")
        song.urlPortada?.let { url ->
            // Si la ruta ya es una URL completa (empieza por http), la usamos tal cual
            val fullUrl = if (url.startsWith("http")) url else baseUrl + url
            
            val glideUrl = GlideUrl(fullUrl, LazyHeaders.Builder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build())

            Glide.with(holder.itemView.context)
                .load(glideUrl)
                .placeholder(R.drawable.portada_generica)
                .error(R.drawable.portada_generica)
                .into(holder.ivSongPortada)
        } ?: run {
            holder.ivSongPortada.setImageResource(R.drawable.portada_generica)
        }

        holder.btnEditSong.setOnClickListener { onEdit(song) }
        holder.btnEditSong.setClickAnimation()
        holder.btnDeleteSong.setOnClickListener { onDelete(song) }
        holder.btnDeleteSong.setClickAnimation()
        holder.itemView.setClickAnimation()
    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<Cancion>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
