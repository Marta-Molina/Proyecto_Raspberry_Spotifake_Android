package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Cancion

interface CancionRepository {

    fun getCanciones(): List<Cancion>

    fun getCancion(position: Int): Cancion

    fun addCancion(cancion: Cancion)

    fun updateCancion(position: Int, cancion: Cancion)

    fun deleteCancion(position: Int)
}

