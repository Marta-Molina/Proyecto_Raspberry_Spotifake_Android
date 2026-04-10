package com.example.appmusica.domain.model

data class Letra(
    val id: Int,
    val cancionId: Int,
    val textoCompleto: String? = null,
    val lineas: List<LetraSync>? = null
)

data class LetraSync(
    val id: Int,
    val letraId: Int,
    val timestamp: Int,
    val texto: String
)
