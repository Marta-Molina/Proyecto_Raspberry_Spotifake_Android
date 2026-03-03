package com.example.appmusica.di

import com.example.appmusica.retrofit.ApiCancionesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val BASE_API_URL = "https://graduation-charms-ethernet-anne.trycloudflare.com/api/"
    const val BASE_STATIC_URL = "https://graduation-charms-ethernet-anne.trycloudflare.com/"

    @Provides
    @Singleton
    fun provideAuthManager(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.example.appmusica.data.local.AuthManager {
        return com.example.appmusica.data.local.AuthManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: com.example.appmusica.retrofit.AuthInterceptor): okhttp3.OkHttpClient {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        return okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: okhttp3.OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiCancionesService {
        return retrofit.create(ApiCancionesService::class.java)
    }
}
