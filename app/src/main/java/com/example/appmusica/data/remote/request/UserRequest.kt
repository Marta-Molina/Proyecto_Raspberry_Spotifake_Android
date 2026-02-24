package com.example.appmusica.data.remote.request

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("correo") val correo: String,
    @SerializedName("pass") val pass: String,
    @SerializedName("admin") val admin: Boolean? = null,
    @SerializedName("premium") val premium: Boolean? = null
)
