package com.example.appmusica.domain.model

data class SolicitudAmistad(
    val id: Int,
    val remitenteId: Long,
    val destinatarioId: Long,
    val estado: String,
    val fecha: String
)
