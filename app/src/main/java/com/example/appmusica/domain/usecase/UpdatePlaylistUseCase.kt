package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Playlist
import com.example.appmusica.domain.repository.PlaylistRepository
import javax.inject.Inject

class UpdatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(id: Int, playlist: Playlist): Playlist? = repository.updateLista(id, playlist)
}
