package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Playlist(
    val id: Int = 0,
    val nombre: String,
    @SerializedName("idUsuario")
    val idUsuario: Int
)
