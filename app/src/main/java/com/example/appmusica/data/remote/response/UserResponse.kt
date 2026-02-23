package com.example.appmusica.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido1") val apellido1: String,
    @SerializedName("apellido2") val apellido2: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("admin") val admin: Boolean,
    @SerializedName("premium") val premium: Boolean,
    @SerializedName("token") val token: String,
    @SerializedName("urlImagen") val urlImagen: String? = null
)
