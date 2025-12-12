package com.example.appmusica

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.appmusica.adapters.AdapterCancion
import com.example.appmusica.models.Cancion

class Controller(val context: Context) {

    companion object {
        const val REQUEST_CODE_UPDATE = 1001
    }
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
        listCanciones.removeAt(position)  // Elimina la canción de la lista
        Toast.makeText(context, "Eliminaste la canción: ${cancion.nombre}", Toast.LENGTH_SHORT).show()
        setAdapter()  // Actualiza el RecyclerView
    }

    // Metodo para editar
    fun updateCancion(position: Int) {
        val cancion = listCanciones[position]
        val intent = Intent(context, EditCancionActivity::class.java)

        intent.putExtra("nombre", cancion.nombre)
        intent.putExtra("artista", cancion.artista)
        intent.putExtra("album", cancion.album)
        intent.putExtra("duracion", cancion.duracion)
        intent.putExtra("imagen", cancion.imagen)
        (context as MainActivity).startActivityForResult(intent, REQUEST_CODE_UPDATE)
    }

    // Metodo para añadir una canción (aquí solo mostramos un Toast)
    fun addCancion() {
        Toast.makeText(context, "Añadiendo una nueva canción", Toast.LENGTH_SHORT).show()
    }
}