package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Artista(
    val id: Int,
    val nombre: String,
    val seguidores: Int = 0,
    val likesTotales: Int = 0,

    @SerializedName("fotoUrl")
    val fotoUrl: String?
)
