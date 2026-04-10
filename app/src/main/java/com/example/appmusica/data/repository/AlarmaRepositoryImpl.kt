package com.example.appmusica.data.repository

import com.example.appmusica.domain.model.Alarma
import com.example.appmusica.domain.repository.AlarmaRepository
import com.example.appmusica.retrofit.ApiCancionesService
import javax.inject.Inject

class AlarmaRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : AlarmaRepository {
    override suspend fun getAlarms(): List<Alarma> {
        val response = api.getAlarms()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun createAlarm(alarm: Alarma): Alarma? {
        val response = api.createAlarm(alarm)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun updateAlarm(id: Int, alarm: Alarma): Boolean {
        return api.updateAlarm(id, alarm).isSuccessful
    }

    override suspend fun deleteAlarm(id: Int): Boolean {
        return api.deleteAlarm(id).isSuccessful
    }
}
