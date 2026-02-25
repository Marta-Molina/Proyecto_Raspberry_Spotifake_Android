package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Album(
    val id: Int,
    val nombre: String,
    val artista: String? = null,
    val artistaId: Int,

    @SerializedName("portadaUrl")
    val portadaUrl: String?
)
