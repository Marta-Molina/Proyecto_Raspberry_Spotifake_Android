package com.example.appmusica.presentation.canciones.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemCancionBinding
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.util.setClickAnimation

class ViewHCancion(
    view: View,
    private val delete: (Int) -> Unit,
    private val update: (Int) -> Unit,
    private val like: (Int) -> Unit,
    private val addToList: (Int) -> Unit,
    private val onRemove: ((Int) -> Unit)? = null,
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
            // BASE_URL already ends with /api/ so just remove the trailing slash.
            // Static files are served under /api/archivos/ due to rootPath in application.yaml
            val baseUrl = NetworkModule.BASE_URL.removeSuffix("/")

            val fullUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath

            val glideUrl = GlideUrl(fullUrl, LazyHeaders.Builder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build())

            Glide.with(binding.ivCancion.context)
                .load(glideUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.portada_generica)
                .error(R.drawable.portada_generica)
                .into(binding.ivCancion)
        } ?: run {
            // Si es null, ponemos una imagen por defecto
            binding.ivCancion.setImageResource(R.drawable.portada_generica)
        }

        // Click normal → detalle
        itemView.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onItemClick(bindingAdapterPosition)
            }
        }
        itemView.setClickAnimation()

        // Botón borrar
        binding.btnDelete.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                delete(bindingAdapterPosition)
            }
        }
        binding.btnDelete.setClickAnimation()

        // Botón editar
        binding.btnEdit.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                update(bindingAdapterPosition)
            }
        }
        binding.btnEdit.setClickAnimation()

        // Botón Like
        binding.btnLike.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                like(bindingAdapterPosition)
            }
        }
        binding.btnLike.setClickAnimation()

        // Botón Add to List vs Remove
        if (onRemove != null) {
            binding.btnRemove.visibility = View.VISIBLE
            binding.btnAddToList.visibility = View.GONE
            binding.btnRemove.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onRemove(bindingAdapterPosition)
                }
            }
        } else {
            binding.btnRemove.visibility = View.GONE
            binding.btnAddToList.visibility = View.VISIBLE
            binding.btnAddToList.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    addToList(bindingAdapterPosition)
                }
            }
            binding.btnAddToList.setClickAnimation()
        }
    }
}
