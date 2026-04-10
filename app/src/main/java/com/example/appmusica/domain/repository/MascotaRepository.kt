package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Mascota

interface MascotaRepository {
    suspend fun getAllMascotas(): List<Mascota>
    suspend fun getUserMascotas(): List<Mascota>
    suspend fun buyMascota(mascotaId: Int): Boolean
    suspend fun setActiveMascota(mascotaId: Int): Boolean
    suspend fun clearActiveMascota(): Boolean
    suspend fun getActiveMascota(): Mascota?
}
