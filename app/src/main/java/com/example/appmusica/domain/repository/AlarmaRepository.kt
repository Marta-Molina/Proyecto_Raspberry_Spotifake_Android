package com.example.appmusica.domain.repository

import com.example.appmusica.domain.model.Alarma

interface AlarmaRepository {
    suspend fun getAlarms(): List<Alarma>
    suspend fun createAlarm(alarm: Alarma): Alarma?
    suspend fun updateAlarm(id: Int, alarm: Alarma): Boolean
    suspend fun deleteAlarm(id: Int): Boolean
}
