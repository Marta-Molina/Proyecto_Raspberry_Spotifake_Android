package com.example.appmusica.retrofit.user

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiServiceInterface {

    @POST("auth")
    suspend fun login(@Body log: RequestUser): Response<ResponseUser>

    @POST("register")
    suspend fun register(@Body log: RequestUser): Response<ResponseUser>


    @GET("employee")
    suspend fun getAll(@Header("Authorization") token: String): Response<List<ResponseUser>>

}