package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Artista(
    val id: Int,
    val nombre: String,

    @SerializedName("fotoUrl")
    val fotoUrl: String?
)
