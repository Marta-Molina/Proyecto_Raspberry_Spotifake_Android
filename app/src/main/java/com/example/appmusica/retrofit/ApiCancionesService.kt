package com.example.appmusica.retrofit

import com.example.appmusica.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiCancionesService {

    @GET("canciones")
    suspend fun getCanciones(
        @Query("nombre") nombre: String? = null,
        @Query("artista") artista: String? = null,
        @Query("album") album: String? = null
    ): Response<List<Cancion>>

    @GET("artistas")
    suspend fun getArtistas(): Response<List<com.example.appmusica.domain.model.Artista>>

    @GET("artistas/{id}/albums")
    suspend fun getAlbumsByArtist(@Path("id") artistId: Int): Response<List<com.example.appmusica.domain.model.Album>>

    @GET("albums/{id}/canciones")
    suspend fun getCancionesByAlbum(
        @Path("id") albumId: Int
    ): Response<List<Cancion>>

    @GET("canciones/{id}")
    suspend fun getCancionById(@Path("id") id: Int): Response<Cancion>

    @POST("canciones")
    suspend fun addCancion(@Body cancion: Cancion): Response<Cancion>

    @Multipart
    @PATCH("canciones/{id}")
    suspend fun updateCancion(
        @Path("id") id: Int,
        @Part("nombre") nombre: okhttp3.RequestBody? = null,
        @Part("artista") artista: okhttp3.RequestBody? = null,
        @Part("album") album: okhttp3.RequestBody? = null,
        @Part("genero") genero: okhttp3.RequestBody? = null,
        @Part("likes") likes: okhttp3.RequestBody? = null,
        @Part("artistaId") artistaId: okhttp3.RequestBody? = null,
        @Part("albumId") albumId: okhttp3.RequestBody? = null
    ): Response<Cancion>

    @DELETE("canciones/{id}")
    suspend fun deleteCancion(@Path("id") id: Int): Response<Unit>

    @PATCH("canciones/{id}/likes")
    suspend fun likeCancion(@Path("id") id: Int): Response<Cancion>

    @PATCH("canciones/{id}/likes/remove")
    suspend fun unlikeCancion(@Path("id") id: Int): Response<Cancion>

    @PATCH("canciones/{id}/reproducciones")
    suspend fun incrementReproducciones(@Path("id") id: Int): Response<Unit>

    @GET("artistas/{id}")
    suspend fun getArtistaById(@Path("id") id: Int): Response<com.example.appmusica.domain.model.Artista>

    @PATCH("artistas/{id}/follow")
    suspend fun followArtista(@Path("id") id: Int): Response<Unit>

    @PATCH("artistas/{id}/unfollow")
    suspend fun unfollowArtista(@Path("id") id: Int): Response<Unit>

    // --- Playlists ---

    @GET("listas")
    suspend fun getListas(): Response<List<com.example.appmusica.domain.model.Playlist>>

    @POST("listas")
    suspend fun createLista(@Body lista: com.example.appmusica.domain.model.Playlist): Response<com.example.appmusica.domain.model.Playlist>

    @PATCH("listas/{id}")
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

    @POST("register")
    suspend fun register(@Body userRequest: com.example.appmusica.data.remote.request.UserRequest): Response<com.example.appmusica.data.remote.response.UserResponse>

    @POST("login")
    suspend fun login(@Body userRequest: com.example.appmusica.data.remote.request.UserRequest): Response<com.example.appmusica.data.remote.response.UserResponse>

    @Multipart
    @PATCH("usuarios/{id}/perfil")
    suspend fun uploadProfileImage(
        @Path("id") userId: Long,
        @Part imagen: okhttp3.MultipartBody.Part
    ): Response<com.example.appmusica.data.remote.response.UserResponse>

    @GET("generos")
    suspend fun getGeneros(): Response<List<com.example.appmusica.domain.model.Genero>>

    @POST("generos")
    suspend fun addGenero(@Body genero: com.example.appmusica.domain.model.Genero): Response<com.example.appmusica.domain.model.Genero>

    @PATCH("generos/{id}")
    suspend fun updateGenero(@Path("id") id: Int, @Body genero: com.example.appmusica.domain.model.Genero): Response<com.example.appmusica.domain.model.Genero>

    @DELETE("generos/{id}")
    suspend fun deleteGenero(@Path("id") id: Int): Response<Unit>

    @GET("usuarios")
    suspend fun getUsuarios(): Response<List<com.example.appmusica.data.remote.response.UserResponse>>

    @PATCH("usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Long,
        @Body updateRequest: com.example.appmusica.data.remote.request.UserRequest
    ): Response<com.example.appmusica.data.remote.response.UserResponse>

    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Long): Response<Unit>

    @GET("ads/random")
    suspend fun getRandomAd(): Response<com.example.appmusica.domain.model.Anuncio>

    // --- Lyrics ---
    @GET("lyrics/{cancionId}")
    suspend fun getLyrics(@Path("cancionId") cancionId: Int): Response<Letra>

    // --- Reproducciones ---
    @POST("reproducir")
    suspend fun registerReproduccion(@Body repro: Reproduccion): Response<Reproduccion>

    @GET("history")
    suspend fun getHistory(): Response<List<Reproduccion>>

    @GET("stats/{year}")
    suspend fun getStats(@Path("year") year: Int): Response<Map<String, Any>>

    // --- Social (Friend Requests) ---
    @POST("social/friend/request/{destinatarioId}")
    suspend fun sendFriendRequest(@Path("destinatarioId") destId: Long): Response<Unit>

    @POST("social/friend/accept/{requestId}")
    suspend fun acceptFriendRequest(@Path("requestId") requestId: Int): Response<Unit>

    @GET("social/friends")
    suspend fun getFriends(): Response<List<Long>>

    // --- Mascotas ---
    @GET("mascotas")
    suspend fun getAllMascotas(): Response<List<Mascota>>

    @GET("mascotas/user")
    suspend fun getUserMascotas(): Response<List<Mascota>>

    @POST("mascotas/buy/{mascotaId}")
    suspend fun buyMascota(@Path("mascotaId") mascotaId: Int): Response<Unit>

    @POST("mascotas/active/{mascotaId}")
    suspend fun setActiveMascota(@Path("mascotaId") mascotaId: Int): Response<Unit>

    @POST("mascotas/active")
    suspend fun clearActiveMascota(): Response<Unit>

    @GET("mascotas/active")
    suspend fun getActiveMascota(): Response<Mascota>

    // --- Alarmas ---
    @GET("alarms")
    suspend fun getAlarms(): Response<List<Alarma>>

    @POST("alarms")
    suspend fun createAlarm(@Body alarm: Alarma): Response<Alarma>

    @PUT("alarms/{id}")
    suspend fun updateAlarm(@Path("id") id: Int, @Body alarm: Alarma): Response<Unit>

    @DELETE("alarms/{id}")
    suspend fun deleteAlarm(@Path("id") id: Int): Response<Unit>

    @GET("usuarios/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<com.example.appmusica.data.remote.response.UserResponse>>
}
