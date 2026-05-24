package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Notificacion(
    val id: Int,
    val idUsuario: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // "friend_request", "friend_accepted", "playlist_shared", "new_release"
    val fecha: String, // LocalDateTime as String
    val leida: Boolean,
    val idReferencia: Int? = null
)
