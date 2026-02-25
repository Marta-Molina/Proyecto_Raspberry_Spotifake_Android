package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class GetCancionesForAlbumUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(artist: String, album: String): List<Cancion> = repository.getCancionesByAlbum(artist, album)
}
