package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Album(
    val id: Int,
    val nombre: String,
    val artistaId: Int? = null,

    @SerializedName("portadaUrl")
    val portadaUrl: String?,

    @SerializedName("fechaLanzamiento")
    val fechaLanzamiento: String? = null
)
