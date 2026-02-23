package com.example.appmusica.presentation.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.domain.model.Genero

class GenreAdapter(
    private var genres: List<Genero>,
    private val onEdit: (Genero) -> Unit,
    private val onDelete: (Genero) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    class GenreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGenreName: TextView = view.findViewById(R.id.tvGenreName)
        val btnEditGenre: View = view.findViewById(R.id.btnEditGenre)
        val btnDeleteGenre: View = view.findViewById(R.id.btnDeleteGenre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genero, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.tvGenreName.text = genre.nombre
        
        holder.btnEditGenre.setOnClickListener { onEdit(genre) }
        holder.btnDeleteGenre.setOnClickListener { onDelete(genre) }
    }

    override fun getItemCount() = genres.size

    fun updateGenres(newGenres: List<Genero>) {
        genres = newGenres
        notifyDataSetChanged()
    }
}
