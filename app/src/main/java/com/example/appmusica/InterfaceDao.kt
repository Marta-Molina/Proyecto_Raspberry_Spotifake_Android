package com.example.appmusica

import com.example.appmusica.models.Cancion

interface InterfaceDao {
    fun getDataCanciones(): List<Cancion> // Método que devuelve las canciones
}