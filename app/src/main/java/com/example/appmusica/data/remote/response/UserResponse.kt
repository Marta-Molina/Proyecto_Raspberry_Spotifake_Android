package com.example.appmusica.data.remote.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("admin") val admin: Boolean,
    @SerializedName("premium") val premium: Boolean,
    @SerializedName("token") val token: String,
    @SerializedName("urlImagen") val urlImagen: String? = null
) : Serializable
