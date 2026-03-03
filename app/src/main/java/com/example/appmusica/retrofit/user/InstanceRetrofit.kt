package com.example.appmusica.retrofit.user

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object InstanceRetrofit {
    private const val URL_BASE = "https://graduation-charms-ethernet-anne.trycloudflare.com/api/"


    val retrofitService : ApiServiceInterface by lazy {
        getRetrofit().create(ApiServiceInterface::class.java)
    }



    private fun getRetrofit() : Retrofit = Retrofit
        .Builder()
        .baseUrl(URL_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}