package com.example.appmusica.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.appmusica.databinding.ItemCancionBinding
import com.example.appmusica.models.Cancion

class ViewHCancion(
    view: View,
    private val delete: (Int) -> Unit,
    private val update: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCancionBinding.bind(view)

    fun renderize(cancion: Cancion) {
        binding.txtviewNombre.text = cancion.nombre
        binding.txtviewArtista.text = cancion.artista
        binding.txtviewAlbum.text = cancion.album
        binding.txtviewDuracion.text = cancion.duracion

        // Cargar la imagen con Glide
        Glide.with(binding.ivCancion.context)
            .load(cancion.imagen) // URL de la canción
            .centerCrop() // Ajuste de la imagen
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache para mejorar rendimiento
            .placeholder(android.R.color.darker_gray) // Mientras carga
            .error(android.R.color.black) // Si falla la carga
            .into(binding.ivCancion)

        // Click normal → detalle
        itemView.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onItemClick(bindingAdapterPosition)
            }
        }

        // Botón borrar
        binding.btnDelete.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                delete(bindingAdapterPosition)
            }
        }

        // Botón editar
        binding.btnEdit.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                update(bindingAdapterPosition)
            }
        }
    }
}
