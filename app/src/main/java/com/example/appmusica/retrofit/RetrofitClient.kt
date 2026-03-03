package com.example.appmusica.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val api: ApiCancionesService by lazy {
        Retrofit.Builder()
            .baseUrl(com.example.appmusica.di.NetworkModule.BASE_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCancionesService::class.java)
    }
}
