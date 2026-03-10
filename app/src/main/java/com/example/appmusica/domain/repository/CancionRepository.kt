package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Cancion

interface CancionRepository {
    suspend fun getCanciones(nombre: String? = null, artista: String? = null, album: String? = null): List<Cancion>
    suspend fun getCancion(id: Int): Cancion?
    suspend fun addCancion(cancion: Cancion)
    suspend fun updateCancion(id: Int, cancion: Cancion)
    suspend fun deleteCancion(id: Int): Boolean
    suspend fun likeCancion(id: Int): Cancion?
    suspend fun unlikeCancion(id: Int): Cancion?
    suspend fun getGeneros(): List<com.example.appmusica.domain.model.Genero>
    suspend fun getArtistas(): List<com.example.appmusica.domain.model.Artista>
    suspend fun getAlbumsByArtist(artistId: Int): List<com.example.appmusica.domain.model.Album>
    suspend fun getCancionesByAlbum(albumId: Int): List<Cancion>
    suspend fun incrementReproducciones(id: Int): Boolean
}
