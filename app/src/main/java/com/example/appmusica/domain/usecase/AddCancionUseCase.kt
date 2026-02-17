package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class AddCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(cancion: Cancion) {
        repository.addCancion(cancion)
    }
}

