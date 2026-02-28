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
}
