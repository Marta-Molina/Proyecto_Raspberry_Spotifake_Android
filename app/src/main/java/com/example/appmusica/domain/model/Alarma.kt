package com.example.appmusica.domain.model

data class Alarma(
    val id: Int = 0,
    val userId: Long,
    val nombre: String,
    val hora: String,
    val cancionId: Int,
    val activo: Boolean = true,
    val dias: String? = null
)
