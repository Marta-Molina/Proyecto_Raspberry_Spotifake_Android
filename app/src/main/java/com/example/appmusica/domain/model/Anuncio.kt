package com.example.appmusica.domain.model

data class Anuncio(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val urlImagen: String,
    val urlDestino: String? = null
)
