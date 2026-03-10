package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Artista

interface ArtistaRepository {
    suspend fun getArtistaById(id: Int): Artista?
    suspend fun followArtista(id: Int): Boolean
    suspend fun unfollowArtista(id: Int): Boolean
}
