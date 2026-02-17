package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Cancion(

    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("artista")
    val artista: String,

    @SerializedName("album")
    val album: String,

    @SerializedName("urlportada")
    val portadaUrl: String,

    @SerializedName("urlaudio")
    val audioUrl: String,

    @SerializedName("genero")
    val genero: String? = null,

    @SerializedName("likes")
    val likes: Int? = null,

    @SerializedName("duracion")
    val duracion: String? = null
)
