package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class GetCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(id: Int): Cancion? {
        return repository.getCancion(id)
    }
}
