package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Genero
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class GetGenerosUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(): List<Genero> {
        return repository.getGeneros()
    }
}
