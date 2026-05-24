package com.example.appmusica.data.repository

import com.example.appmusica.domain.model.Letra
import com.example.appmusica.domain.model.SolicitudAmistad
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

    override suspend fun getPendingRequests(): List<SolicitudAmistad> {
        val response = api.getPendingRequests()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getSentRequests(): List<SolicitudAmistad> {
        val response = api.getSentRequests()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun rejectFriendRequest(reqId: Int): Boolean {
        return api.rejectFriendRequest(reqId).isSuccessful
    }

    override suspend fun getUsuarioById(id: Long): com.example.appmusica.data.remote.response.UserResponse? {
        val response = api.getUsuarioById(id)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun likeCancion(cancionId: Int): Boolean {
        return api.socialLikeCancion(cancionId).isSuccessful
    }

    override suspend fun unlikeCancion(cancionId: Int): Boolean {
        return api.socialUnlikeCancion(cancionId).isSuccessful
    }

    override suspend fun followArtista(artistaId: Int): Boolean {
        return api.socialFollowArtista(artistaId).isSuccessful
    }

    override suspend fun unfollowArtista(artistaId: Int): Boolean {
        return api.socialUnfollowArtista(artistaId).isSuccessful
    }
}
