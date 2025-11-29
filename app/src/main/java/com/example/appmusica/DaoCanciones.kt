package com.example.appmusica

import com.example.appmusica.models.Cancion
import com.example.appmusica.objects_models.Repository

class DaoCanciones private constructor(): InterfaceDao {

    companion object {
        val myDao: DaoCanciones by lazy { DaoCanciones() } // Instancia única
    }

    // Metodo que devuelve la lista de canciones del repositorio
    override fun getDataCanciones(): List<Cancion> = Repository.listCanciones
}
