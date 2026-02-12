package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class GetCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    operator fun invoke(position: Int): Cancion {
        return repository.getCancion(position)
    }
}
