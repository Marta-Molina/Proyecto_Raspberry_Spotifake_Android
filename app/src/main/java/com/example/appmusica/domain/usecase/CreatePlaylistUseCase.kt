package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Playlist
import com.example.appmusica.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlist: Playlist): Playlist? = repository.createLista(playlist)
}
