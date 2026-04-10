package com.example.appmusica.data.repository

import com.example.appmusica.domain.model.Letra
import com.example.appmusica.domain.repository.SocialRepository
import com.example.appmusica.retrofit.ApiCancionesService
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : SocialRepository {
    override suspend fun getLyrics(cancionId: Int): Letra? {
        val response = api.getLyrics(cancionId)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun sendFriendRequest(destId: Long): Boolean {
        return api.sendFriendRequest(destId).isSuccessful
    }

    override suspend fun acceptFriendRequest(reqId: Int): Boolean {
        return api.acceptFriendRequest(reqId).isSuccessful
    }

    override suspend fun getFriends(): List<Long> {
        val response = api.getFriends()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
}
