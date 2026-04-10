package com.example.appmusica.data.repository

import com.example.appmusica.domain.model.Mascota
import com.example.appmusica.domain.repository.MascotaRepository
import com.example.appmusica.retrofit.ApiCancionesService
import javax.inject.Inject

class MascotaRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : MascotaRepository {
    override suspend fun getAllMascotas(): List<Mascota> {
        val response = api.getAllMascotas()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getUserMascotas(): List<Mascota> {
        val response = api.getUserMascotas()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun buyMascota(mascotaId: Int): Boolean {
        return api.buyMascota(mascotaId).isSuccessful
    }

    override suspend fun setActiveMascota(mascotaId: Int): Boolean {
        return api.setActiveMascota(mascotaId).isSuccessful
    }

    override suspend fun clearActiveMascota(): Boolean {
        return api.clearActiveMascota().isSuccessful
    }

    override suspend fun getActiveMascota(): Mascota? {
        val response = api.getActiveMascota()
        return if (response.isSuccessful) response.body() else null
    }
}
