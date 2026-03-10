package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Cancion(
    val id: Int,
    val nombre: String,
    val artistasIds: List<Int>? = emptyList(),
    val albumesIds: List<Int>? = emptyList(),
    val artistas: List<String>? = emptyList(),
    val albumes: List<String>? = emptyList(),
    val genero: Int,
    val likes: Int,
    val reproducciones: Int = 0,

    @SerializedName("urlAudio")
    val urlAudio: String?,

    @SerializedName("urlPortada")
    val urlPortada: String?
)

