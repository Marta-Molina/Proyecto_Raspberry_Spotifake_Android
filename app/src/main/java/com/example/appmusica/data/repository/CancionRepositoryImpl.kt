package com.example.appmusica.data.repository

import com.example.appmusica.data.datasource.FakeCancionesDataSource
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import com.example.appmusica.retrofit.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancionRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dataSource: FakeCancionesDataSource
) : CancionRepository {

    override suspend fun getCanciones(): List<Cancion> {
        return try {
            val response = api.getCanciones()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                dataSource.canciones
            }
        } catch (e: Exception) {
            dataSource.canciones
        }
    }

    override suspend fun getCancion(id: Int): Cancion? {
        return try {
            val response = api.getCancionById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                dataSource.canciones.find { it.id == id }
            }
        } catch (e: Exception) {
            dataSource.canciones.find { it.id == id }
        }
    }

    override suspend fun addCancion(cancion: Cancion) {
        // Implement API call if needed, currently adding to fake
        dataSource.canciones.add(cancion)
    }

    override suspend fun updateCancion(id: Int, cancion: Cancion) {
        val index = dataSource.canciones.indexOfFirst { it.id == id }
        if (index != -1) dataSource.canciones[index] = cancion
    }

    override suspend fun deleteCancion(id: Int) {
        api.deleteCancion(id)
        dataSource.canciones.removeAll { it.id == id }
    }
}
