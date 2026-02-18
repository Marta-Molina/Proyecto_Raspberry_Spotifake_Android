package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.model.Playlist

interface PlaylistRepository {
    suspend fun getListas(): List<Playlist>
    suspend fun createLista(playlist: Playlist): Playlist?
    suspend fun updateLista(id: Int, playlist: Playlist): Playlist?
    suspend fun deleteLista(id: Int)
    suspend fun getUserListas(userId: Int): List<Playlist>
    suspend fun getListaCanciones(listaId: Int): List<Cancion>
    suspend fun addCancionToLista(listaId: Int, cancionId: Int)
    suspend fun removeCancionFromLista(listaId: Int, cancionId: Int)
}
