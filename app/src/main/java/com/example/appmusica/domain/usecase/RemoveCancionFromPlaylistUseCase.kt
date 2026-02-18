package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveCancionFromPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Int, cancionId: Int) = repository.removeCancionFromLista(playlistId, cancionId)
}
