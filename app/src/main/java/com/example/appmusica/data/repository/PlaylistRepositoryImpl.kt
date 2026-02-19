package com.example.appmusica.data.repository

import android.util.Log
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.model.Playlist
import com.example.appmusica.domain.repository.PlaylistRepository
import com.example.appmusica.retrofit.ApiCancionesService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : PlaylistRepository {

    override suspend fun getListas(): List<Playlist> {
        return getUserListas(1)
    }

    override suspend fun createLista(playlist: Playlist): Playlist? {
        return try {
            val response = api.createLista(playlist)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_TEST", "Error creating lista: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception creating lista: ${e.message}")
            null
        }
    }

    override suspend fun updateLista(id: Int, playlist: Playlist): Playlist? {
        return try {
            val response = api.updateLista(id, playlist)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_TEST", "Error updating lista $id: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception updating lista $id: ${e.message}")
            null
        }
    }

    override suspend fun deleteLista(id: Int) {
        try {
            val response = api.deleteLista(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error deleting lista $id: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception deleting lista $id: ${e.message}")
        }
    }

    override suspend fun getUserListas(userId: Int): List<Playlist> {
        return try {
            val response = api.getUserListas(userId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching user listas: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching user listas: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getListaCanciones(listaId: Int): List<Cancion> {
        return try {
            val response = api.getListaCanciones(listaId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching lista canciones: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching lista canciones: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addCancionToLista(listaId: Int, cancionId: Int) {
        try {
            val body = mapOf("idCancion" to cancionId)
            Log.d("API_LISTAS", "Adding cancion $cancionId to lista $listaId")
            val response = api.addCancionToLista(listaId, body)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error adding cancion to lista: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception adding cancion to lista: ${e.message}")
        }
    }

    override suspend fun removeCancionFromLista(listaId: Int, cancionId: Int) {
        try {
            val response = api.removeCancionFromLista(listaId, cancionId)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error removing cancion from lista: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception removing cancion from lista: ${e.message}")
        }
    }
}
