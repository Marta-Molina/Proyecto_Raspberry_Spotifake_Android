package com.example.appmusica.viewholder

import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.databinding.ItemCancionBinding
import com.example.appmusica.fragments.CancionesFragment
import com.example.appmusica.models.Cancion

class ViewHCancion(
    view: View,
    private val delete: (Int) -> Unit,
    private val update: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCancionBinding.bind(view)

    fun renderize(cancion: Cancion) {
        binding.txtviewNombre.text = cancion.nombre
        binding.txtviewArtista.text = cancion.artista
        binding.txtviewAlbum.text = cancion.album
        binding.txtviewDuracion.text = cancion.duracion

        Glide.with(itemView.context)
            .load(cancion.imagen)
            .into(binding.ivCancion)

        itemView.setOnClickListener {
            val action =
                CancionesFragment.actionCancionesFragmentToDetalleFragment(adapterPosition)
            it.findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener { delete(adapterPosition) }
        binding.btnEdit.setOnClickListener { update(adapterPosition) }
    }
}
