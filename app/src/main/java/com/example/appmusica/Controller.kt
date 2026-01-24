package com.example.appmusica

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.adapters.AdapterCancion
import com.example.appmusica.models.Cancion

class Controller(
    private val context: Context,
    private val onItemClick: (Int) -> Unit   //NUEVO callback
) {

    private val listCanciones: MutableList<Cancion>

    init {
        listCanciones = DaoCanciones.myDao.getDataCanciones().toMutableList()
    }

    fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = AdapterCancion(
            listCanciones,
            { pos -> delCancion(pos) },     // borrar
            { pos -> updateCancion(pos) },  // editar
            { pos -> onItemClick(pos) }     //click normal → detalle
        )
    }

    private fun delCancion(position: Int) {
        val cancion = listCanciones[position]
        listCanciones.removeAt(position)
        Toast.makeText(
            context,
            "Eliminada: ${cancion.nombre}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateCancion(position: Int) {
        val intent = Intent(context, EditCancionActivity::class.java)
        intent.putExtra("pos", position)
        context.startActivity(intent)
    }
}

