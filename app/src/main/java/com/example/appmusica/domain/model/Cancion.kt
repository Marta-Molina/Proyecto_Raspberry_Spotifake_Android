package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Cancion(
    val id: Int,
    val nombre: String,
    val artistaIds: List<Int> = emptyList(),
    val albumId: Int? = null,
    val generosIds: List<Int> = emptyList(),
    val likes: Int = 0,
    val reproducciones: Int = 0,

    // Campos informativos para el cliente
    val artista: String? = null,
    val album: String? = null,
    val genero: Int? = null,

    @SerializedName("urlAudio")
    val urlAudio: String?,

    @SerializedName("urlPortada")
    val urlPortada: String?
)

