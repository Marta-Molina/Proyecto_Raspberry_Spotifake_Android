package com.example.appmusica.domain.model

data class Cancion(
    val id: Int,
    val nombre: String,
    val artista: String,
    val album: String,
    val duracion: String? = null,
    val portadaUrl: String,
    val audioUrl: String
)
