package com.example.appmusica.data.repository

import android.util.Log
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import com.example.appmusica.retrofit.ApiCancionesService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancionRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : CancionRepository {

    override suspend fun getCanciones(nombre: String?, artista: String?, album: String?): List<Cancion> {
        return try {
            val response = api.getCanciones(nombre, artista, album)
            if (response.isSuccessful) {
                Log.d("API_TEST", response.body().toString())
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching canciones: ${response.code()} ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching canciones: ${e.message}", e)
            emptyList()
        }
    }


    override suspend fun getCancion(id: Int): Cancion? {
        return try {
            val response = api.getCancionById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_TEST", "Error fetching cancion $id: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching cancion $id: ${e.message}")
            null
        }
    }

    override suspend fun addCancion(cancion: Cancion) {
        try {
            val response = api.addCancion(cancion)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error adding cancion: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception adding cancion: ${e.message}")
        }
    }

    override suspend fun updateCancion(id: Int, cancion: Cancion) {
        try {
            val response = api.updateCancion(id, cancion)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error updating cancion $id: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception updating cancion $id: ${e.message}")
        }
    }

    override suspend fun deleteCancion(id: Int) {
        try {
            val response = api.deleteCancion(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error deleting cancion $id: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception deleting cancion $id: ${e.message}")
        }
    }
}
