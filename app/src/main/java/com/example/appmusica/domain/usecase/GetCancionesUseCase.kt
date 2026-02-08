package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.repository.CancionRepository
import com.example.appmusica.domain.model.Cancion
import javax.inject.Inject

class GetCancionesUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    operator fun invoke(): List<Cancion> {
        return repository.getCanciones()
    }
}

