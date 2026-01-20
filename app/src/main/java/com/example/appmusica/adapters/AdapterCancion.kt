package com.example.appmusica.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.viewholder.ViewHCancion
import com.example.appmusica.models.Cancion

class AdapterCancion(
    private val list: MutableList<Cancion>,
    private val delete: (Int) -> Unit,
    private val update: (Int) -> Unit
) : RecyclerView.Adapter<ViewHCancion>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHCancion {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancion, parent, false)
        return ViewHCancion(view, delete, update)
    }

    override fun onBindViewHolder(holder: ViewHCancion, position: Int) {
        holder.renderize(list[position])
    }

    override fun getItemCount(): Int = list.size
}
