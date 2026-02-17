package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class UpdateCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(id: Int, cancion: Cancion) {
        repository.updateCancion(id, cancion)
    }
}
