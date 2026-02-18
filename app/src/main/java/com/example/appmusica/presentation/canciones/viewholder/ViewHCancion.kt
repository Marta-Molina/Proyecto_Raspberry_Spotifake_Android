package com.example.appmusica.presentation.canciones.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemCancionBinding
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.domain.model.Cancion

class ViewHCancion(
    view: View,
    private val delete: (Int) -> Unit,
    private val update: (Int) -> Unit,
    private val like: (Int) -> Unit,
    private val addToList: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCancionBinding.bind(view)

    fun renderize(cancion: Cancion) {

        binding.txtviewNombre.text = cancion.nombre
        binding.txtviewArtista.text = cancion.artista
        binding.txtviewAlbum.text = cancion.album
        binding.txtviewLikes.text = cancion.likes.toString()

        // Cambiar icono de like si tiene likes (demo simple)
        if (cancion.likes > 0) {
            binding.btnLike.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            binding.btnLike.setImageResource(android.R.drawable.btn_star_big_off)
        }

        // ✅ Cargar imagen solo si no es null
        cancion.urlPortada?.let { portadaPath ->

            val fullUrl =
                NetworkModule.BASE_URL + portadaPath.removePrefix("/")

            Glide.with(binding.ivCancion.context)
                .load(fullUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.white)
                .error(R.color.black)
                .into(binding.ivCancion)
        } ?: run {
            // Si es null, ponemos una imagen por defecto
            binding.ivCancion.setImageResource(R.color.white)
        }

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

        // Botón Like
        binding.btnLike.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                like(bindingAdapterPosition)
            }
        }

        // Botón Add to List
        binding.btnAddToList.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                addToList(bindingAdapterPosition)
            }
        }
    }
}
