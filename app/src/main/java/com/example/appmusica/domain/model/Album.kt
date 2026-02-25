package com.example.appmusica.domain.model

data class Album(
    val id: Int,
    val nombre: String,
    val artista: String? = null,
    val artistaId: Int,
    val portadaUrl: String?
)
