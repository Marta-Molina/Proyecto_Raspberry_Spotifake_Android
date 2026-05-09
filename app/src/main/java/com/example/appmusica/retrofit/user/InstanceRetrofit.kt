package com.example.appmusica.retrofit.user

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object InstanceRetrofit {
    private val URL_BASE = com.example.appmusica.di.NetworkModule.BASE_API_URL


    val retrofitService : ApiServiceInterface by lazy {
        getRetrofit().create(ApiServiceInterface::class.java)
    }



    private fun getRetrofit() : Retrofit = Retrofit
        .Builder()
        .baseUrl(URL_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}