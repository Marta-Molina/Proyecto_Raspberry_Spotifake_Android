package com.example.appmusica.retrofit

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): Response<List<PostResponse>>

    @POST("posts")
    suspend fun createPost(
        @Body post: PostRequest
    ): Response<PostResponse>

    //SI LA API USA X-WWW-FORM-URLENCODED
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    //ENVIAR HEADERS
    @POST("posts")
    suspend fun createPostWithAuth(
        @Header("Authorization") token: String,
        @Body post: PostRequest
    ): Response<PostResponse>


    /*USO DE LO DE ENVIAR HEADERS
    RetrofitClient.api.createPost(
    "Bearer TU_TOKEN_AQUI",
    newPost
    )
    */

    @GET("canciones")
    suspend fun getCanciones(): Response<List<Cancion>>

    @GET("canciones/{id}")
    suspend fun getCancionById(
        @retrofit2.http.Path("id") id: Int
    ): Response<Cancion>

    @GET("canciones")
    suspend fun getCancionesByArtista(
        @retrofit2.http.Query("artista") artista: String
    ): Response<List<Cancion>>

    @retrofit2.http.DELETE("canciones/{id}")
    suspend fun deleteCancion(
        @retrofit2.http.Path("id") id: Int
    ): Response<Unit>

}