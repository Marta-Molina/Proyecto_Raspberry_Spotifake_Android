package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.PlaylistRepository
import javax.inject.Inject

class GetPlaylistCancionesUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Int): List<Cancion> = repository.getListaCanciones(playlistId)
}
