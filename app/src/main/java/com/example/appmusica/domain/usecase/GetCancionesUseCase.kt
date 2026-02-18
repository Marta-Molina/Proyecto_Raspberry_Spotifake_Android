package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.repository.CancionRepository
import com.example.appmusica.domain.model.Cancion
import javax.inject.Inject

class GetCancionesUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(nombre: String? = null, artista: String? = null, album: String? = null): List<Cancion> {
        return repository.getCanciones(nombre, artista, album)
    }
}

