package com.example.appmusica.retrofit

import com.example.appmusica.domain.model.Cancion
import retrofit2.Response
import retrofit2.http.*

interface ApiCancionesService {

    @GET("canciones")
    suspend fun getCanciones(): Response<List<Cancion>>

    @GET("canciones/{id}")
    suspend fun getCancionById(@Path("id") id: Int): Response<Cancion>

    @DELETE("canciones/{id}")
    suspend fun deleteCancion(@Path("id") id: Int): Response<Unit>
}
