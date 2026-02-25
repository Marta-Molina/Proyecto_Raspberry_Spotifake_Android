package com.example.appmusica.data.remote.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("admin") val admin: Int,
    @SerializedName("premium") val premium: Int,
    @SerializedName("token") val token: String,
    @SerializedName("urlImagen") val urlImagen: String? = null
) : Serializable
