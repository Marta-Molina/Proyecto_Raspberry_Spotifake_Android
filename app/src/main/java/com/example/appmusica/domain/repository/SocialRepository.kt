package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Letra
import com.example.appmusica.domain.model.SolicitudAmistad

interface SocialRepository {
    suspend fun getLyrics(cancionId: Int): Letra?
    suspend fun sendFriendRequest(destId: Long): Boolean
    suspend fun acceptFriendRequest(reqId: Int): Boolean
    suspend fun getFriends(): List<Long>
}
