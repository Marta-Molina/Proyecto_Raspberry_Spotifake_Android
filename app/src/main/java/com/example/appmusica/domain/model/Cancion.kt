package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Cancion(
    val id: Int,
    val nombre: String,
    val artistaId: Int? = null,
    val albumId: Int? = null,
    val artista: String,
    val album: String,
    val genero: Int,
    val likes: Int,

    @SerializedName("urlAudio")
    val urlAudio: String?,

    @SerializedName("urlPortada")
    val urlPortada: String?
)

