package com.example.appmusica.data.repository

import android.util.Log
import com.example.appmusica.domain.model.Artista
import com.example.appmusica.domain.repository.ArtistaRepository
import com.example.appmusica.retrofit.ApiCancionesService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistaRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : ArtistaRepository {

    override suspend fun getArtistaById(id: Int): Artista? {
        return try {
            val response = api.getArtistaById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_TEST", "Error fetching artista $id: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching artista $id: ${e.message}")
            null
        }
    }

    override suspend fun followArtista(id: Int): Boolean {
        return try {
            val response = api.followArtista(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error following artista $id: ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception following artista $id: ${e.message}")
            false
        }
    }

    override suspend fun unfollowArtista(id: Int): Boolean {
        return try {
            val response = api.unfollowArtista(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error unfollowing artista $id: ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception unfollowing artista $id: ${e.message}")
            false
        }
    }
}
