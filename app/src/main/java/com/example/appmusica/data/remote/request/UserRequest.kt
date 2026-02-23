package com.example.appmusica.data.remote.request

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellido1") val apellido1: String? = null,
    @SerializedName("apellido2") val apellido2: String? = null,
    @SerializedName("correo") val correo: String,
    @SerializedName("pass") val pass: String,
    @SerializedName("admin") val admin: Boolean? = null,
    @SerializedName("premium") val premium: Boolean? = null
)
