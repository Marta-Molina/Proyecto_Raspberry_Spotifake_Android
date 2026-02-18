package com.example.appmusica.retrofit

import com.example.appmusica.domain.model.Cancion
import retrofit2.Response
import retrofit2.http.*

interface ApiCancionesService {

    @GET("canciones")
    suspend fun getCanciones(
        @Query("nombre") nombre: String? = null,
        @Query("artista") artista: String? = null,
        @Query("album") album: String? = null
    ): Response<List<Cancion>>

    @GET("canciones/{id}")
    suspend fun getCancionById(@Path("id") id: Int): Response<Cancion>

    @POST("canciones")
    suspend fun addCancion(@Body cancion: Cancion): Response<Cancion>

    @PUT("canciones/{id}")
    suspend fun updateCancion(@Path("id") id: Int, @Body cancion: Cancion): Response<Cancion>

    @DELETE("canciones/{id}")
    suspend fun deleteCancion(@Path("id") id: Int): Response<Unit>

    // --- Playlists ---

    @GET("listas")
    suspend fun getListas(): Response<List<com.example.appmusica.domain.model.Playlist>>

    @POST("listas")
    suspend fun createLista(@Body lista: com.example.appmusica.domain.model.Playlist): Response<com.example.appmusica.domain.model.Playlist>

    @PUT("listas/{id}")
    suspend fun updateLista(@Path("id") id: Int, @Body lista: com.example.appmusica.domain.model.Playlist): Response<com.example.appmusica.domain.model.Playlist>

    @DELETE("listas/{id}")
    suspend fun deleteLista(@Path("id") id: Int): Response<Unit>

    @GET("usuarios/{id}/listas")
    suspend fun getUserListas(@Path("id") userId: Int): Response<List<com.example.appmusica.domain.model.Playlist>>

    @GET("listas/{id}/canciones")
    suspend fun getListaCanciones(@Path("id") listaId: Int): Response<List<Cancion>>

    @POST("listas/{id}/canciones")
    suspend fun addCancionToLista(@Path("id") listaId: Int, @Body body: Map<String, Int>): Response<Unit>

    @DELETE("listas/{idLista}/canciones/{idCancion}")
    suspend fun removeCancionFromLista(@Path("idLista") listaId: Int, @Path("idCancion") cancionId: Int): Response<Unit>
}
