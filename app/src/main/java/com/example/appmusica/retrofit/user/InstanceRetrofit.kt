package com.example.appmusica.retrofit.user

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object InstanceRetrofit {
    private const val URL_BASE = "https://subpatronal-healthiest-kash.ngrok-free.dev/"


    val retrofitService : ApiServiceInterface by lazy {
        getRetrofit().create(ApiServiceInterface::class.java)
    }



    private fun getRetrofit() : Retrofit = Retrofit
        .Builder()
        .baseUrl(URL_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}