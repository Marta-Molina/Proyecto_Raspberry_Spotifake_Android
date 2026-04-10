package com.example.appmusica.domain.model

data class Reproduccion(
    val id: Int = 0,
    val idUsuario: Long,
    val idCancion: Int,
    val fecha: String, // String for simplicity in JSON transport
    val segundosEscuchados: Int
)
