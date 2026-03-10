package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class Album(
    val id: Int,
    val nombre: String,
    val artistasNombre: List<String>? = emptyList(),
    val artistasIds: List<Int>? = emptyList(),

    @SerializedName("portadaUrl")
    val portadaUrl: String?
)
