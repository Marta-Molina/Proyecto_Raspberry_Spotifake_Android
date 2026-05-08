package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Artista

interface ArtistaRepository {
    suspend fun getArtistaById(id: Int): Artista?
    suspend fun followArtista(id: Int): Boolean
    suspend fun unfollowArtista(id: Int): Boolean
    suspend fun createArtista(nombre: String, foto: java.io.File? = null): Artista?
    suspend fun updateArtista(id: Int, nombre: String? = null, foto: java.io.File? = null): Artista?
    suspend fun deleteArtista(id: Int): Boolean
    suspend fun incrementFollowers(id: Int): Boolean
    suspend fun decrementFollowers(id: Int): Boolean
}
