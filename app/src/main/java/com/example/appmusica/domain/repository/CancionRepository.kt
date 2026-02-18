package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Cancion

interface CancionRepository {
    suspend fun getCanciones(nombre: String? = null, artista: String? = null, album: String? = null): List<Cancion>
    suspend fun getCancion(id: Int): Cancion?
    suspend fun addCancion(cancion: Cancion)
    suspend fun updateCancion(id: Int, cancion: Cancion)
    suspend fun deleteCancion(id: Int)
}
