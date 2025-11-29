package com.example.appmusica.adapters

import com.example.appmusica.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.ViewHCancion
import com.example.appmusica.models.Cancion

class AdapterCancion(
    var listCancion: MutableList<Cancion>,
    var deleteOnClick: (Int) -> Unit, // Eliminar
    var updateOnClick: (Int) -> Unit // Actualizar
) : RecyclerView.Adapter<ViewHCancion>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHCancion {
        val layoutInflater = LayoutInflater.from(parent.context)
        val layoutItemCancion = R.layout.item_cancion // Usa el layout para la canción
        return ViewHCancion(
            layoutInflater.inflate(layoutItemCancion, parent, false),
            deleteOnClick,
            updateOnClick
        )
    }

    override fun onBindViewHolder(holder: ViewHCancion, position: Int) {
        holder.renderize(listCancion[position])
    }

    override fun getItemCount(): Int = listCancion.size
}