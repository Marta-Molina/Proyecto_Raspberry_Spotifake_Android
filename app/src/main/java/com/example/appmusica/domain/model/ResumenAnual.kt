package com.example.appmusica.domain.model

import com.google.gson.annotations.SerializedName

data class ResumenAnual(
    @SerializedName("totalTimeSeconds")
    val totalTimeSeconds: Int,
    @SerializedName("totalReproductions")
    val totalReproductions: Int,
    @SerializedName("topSongIds")
    val topSongIds: List<Int>,
    @SerializedName("topArtistIds")
    val topArtistIds: List<Int>
)
