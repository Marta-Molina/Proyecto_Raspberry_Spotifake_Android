package com.example.appmusica.data.repository

import com.example.appmusica.data.datasource.FakeCancionesDataSource
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancionRepositoryImpl @Inject constructor(
    private val dataSource: FakeCancionesDataSource
) : CancionRepository {

    override fun getCanciones(): List<Cancion> {
        return dataSource.canciones
    }

    override fun getCancion(position: Int): Cancion {
        return dataSource.canciones[position]
    }

    override fun addCancion(cancion: Cancion) {
        dataSource.canciones.add(cancion)
    }

    override fun updateCancion(position: Int, cancion: Cancion) {
        dataSource.canciones[position] = cancion
    }

    override fun deleteCancion(position: Int) {
        dataSource.canciones.removeAt(position)
    }
}
