package com.example.appmusica.presentation.canciones.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.presentation.canciones.viewholder.ViewHCancion

class AdapterCancion(
    private val list: MutableList<Cancion>,
    private val delete: (Int) -> Unit,
    private val update: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ViewHCancion>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHCancion {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancion, parent, false)
        return ViewHCancion(view, delete, update, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHCancion, position: Int) {
        holder.renderize(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun submitList(newList: List<Cancion>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateList(newList: List<Cancion>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}
