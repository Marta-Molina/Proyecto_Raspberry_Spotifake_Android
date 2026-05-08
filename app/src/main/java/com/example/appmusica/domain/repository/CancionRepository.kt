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
    suspend fun getStats(year: Int): com.example.appmusica.domain.model.ResumenAnual?
    suspend fun getAlbums(nombre: String? = null): List<com.example.appmusica.domain.model.Album>
    suspend fun getAlbumById(id: Int): com.example.appmusica.domain.model.Album?
    suspend fun createAlbum(artistaId: Int, nombre: String, portada: java.io.File? = null): com.example.appmusica.domain.model.Album?
    suspend fun updateAlbum(id: Int, nombre: String? = null, artistaId: Int? = null, portada: java.io.File? = null): com.example.appmusica.domain.model.Album?
    suspend fun deleteAlbum(id: Int): Boolean
}
