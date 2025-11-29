package com.example.appmusica

import android.content.Context
import android.widget.Toast
import com.example.appmusica.adapters.AdapterCancion
import com.example.appmusica.models.Cancion

class Controller(val context: Context) {
    lateinit var listCanciones: MutableList<Cancion>

    init {
        initData()
    }

    // Inicializa los datos cargando la lista de canciones
    fun initData() {
        listCanciones = DaoCanciones.myDao.getDataCanciones().toMutableList() // Accede al DAO de canciones
    }

    // Metodo para mostrar las canciones en la consola
    fun loggOut() {
        Toast.makeText(context, "He mostrado las canciones en pantalla", Toast.LENGTH_LONG).show()
        listCanciones.forEach {
            println(it)
        }
    }

    // Establece el adaptador para el RecyclerView
    fun setAdapter() {
        val myActivity = context as MainActivity
        myActivity.binding.myRecyclerView.adapter = AdapterCancion(
            listCanciones,
            { pos -> delCancion(pos) }, // Eliminar canción
            { pos -> updateCancion(pos) } // Actualizar canción
        )
    }

    // Metodo para eliminar una canción
    fun delCancion(position: Int) {
        val cancion = listCanciones[position]
        listCanciones.removeAt(position) // Elimina la canción de la lista
        Toast.makeText(context, "Eliminaste la canción: ${cancion.nombre}", Toast.LENGTH_SHORT).show()
        setAdapter() // Actualiza el RecyclerView
    }

    // Metodo para mostrar un Toast indicando que se está editando una canción
    fun updateCancion(position: Int) {
        val cancion = listCanciones[position]
        Toast.makeText(context, "Editando la canción: ${cancion.nombre}", Toast.LENGTH_SHORT).show()
    }

    // Metodo para añadir una canción (aquí solo mostramos un Toast)
    fun addCancion() {
        Toast.makeText(context, "Añadiendo una nueva canción", Toast.LENGTH_SHORT).show()
    }
}