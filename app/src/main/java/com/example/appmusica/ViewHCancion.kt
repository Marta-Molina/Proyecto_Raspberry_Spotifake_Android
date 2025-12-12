package com.example.appmusica

import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.databinding.ItemCancionBinding
import com.example.appmusica.models.Cancion

class ViewHCancion(
    view: View,
    var deleteOnClick: (Int) -> Unit,
    var updateOnClick: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCancionBinding.bind(view)

    fun renderize(cancion: Cancion) {
        // Mapear las propiedades del modelo Cancion a los elementos de la vista
        binding.txtviewNombre.text = cancion.nombre
        binding.txtviewArtista.text = cancion.artista
        binding.txtviewAlbum.text = cancion.album
        binding.txtviewDuracion.text = cancion.duracion

        // Usar Glide para cargar la imagen
        Glide.with(itemView.context)
            .load(cancion.imagen)
            .centerCrop()
            .into(binding.ivCancion)

        // Asignar los listeners a los botones
        setOnClickListener(adapterPosition)
    }

    private fun setOnClickListener(position: Int) {
        binding.btnEdit.setOnClickListener {
            updateOnClick(position) // Llamada a la función de actualización
        }
        binding.btnDelete.setOnClickListener {
            deleteOnClick(position) // Llamada a la función de eliminación
        }
    }


    //confirmación para eliminar
    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(itemView.context)
        builder.setTitle("Confirmar eliminación")
        builder.setMessage("¿Estás seguro de que deseas eliminar esta canción?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            deleteOnClick(position)  // Eliminar la canción si el usuario confirma
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

}
