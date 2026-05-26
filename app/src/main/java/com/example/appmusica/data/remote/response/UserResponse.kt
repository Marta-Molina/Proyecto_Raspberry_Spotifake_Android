package com.example.appmusica.data.remote.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("admin") val admin: Boolean? = false,
    @SerializedName("premium") val premium: Boolean? = false,
    @SerializedName("pass") val pass: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("urlImagen") val urlImagen: String? = null
) : Serializable
