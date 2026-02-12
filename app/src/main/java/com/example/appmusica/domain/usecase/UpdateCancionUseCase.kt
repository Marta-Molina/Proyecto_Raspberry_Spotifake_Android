package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class UpdateCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    operator fun invoke(position: Int, cancion: Cancion) {
        repository.updateCancion(position, cancion)
    }
}
