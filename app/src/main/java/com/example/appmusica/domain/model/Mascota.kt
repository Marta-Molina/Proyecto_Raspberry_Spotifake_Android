package com.example.appmusica.domain.model

data class Mascota(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val urlSprite: String,
    val premiumDefault: Boolean,
    val tipoId: Int
)
