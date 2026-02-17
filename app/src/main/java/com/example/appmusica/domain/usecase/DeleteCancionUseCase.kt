package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class DeleteCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteCancion(id)
    }
}
