package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class DeleteCancionUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    operator fun invoke(position: Int) {
        repository.deleteCancion(position)
    }
}
