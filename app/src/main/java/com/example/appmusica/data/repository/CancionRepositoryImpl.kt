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

    override suspend fun getCanciones(): List<Cancion> {
        return try {
            val response = api.getCanciones()
            if (response.isSuccessful) {
                Log.d("API_TEST", response.body().toString())
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", e.message ?: "Error")
            emptyList()
        }
    }


    override suspend fun getCancion(id: Int): Cancion? {
        return try {
            val response = api.getCancionById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addCancion(cancion: Cancion) {
        // Implementation for adding cancion to API could go here
    }

    override suspend fun updateCancion(id: Int, cancion: Cancion) {
        // Implementation for updating cancion via API could go here
    }

    override suspend fun deleteCancion(id: Int) {
        try {
            api.deleteCancion(id)
        } catch (e: Exception) {
            // Handle error
        }
    }
}
