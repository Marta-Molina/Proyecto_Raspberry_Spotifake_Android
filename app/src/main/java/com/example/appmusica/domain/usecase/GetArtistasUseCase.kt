package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Artista
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class GetArtistasUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(): List<Artista> = repository.getArtistas()
}
