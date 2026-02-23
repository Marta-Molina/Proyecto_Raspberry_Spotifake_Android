package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Playlist
import com.example.appmusica.domain.repository.PlaylistRepository
import javax.inject.Inject

class GetUserPlaylistsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(userId: Int): List<Playlist> = repository.getUserListas(userId)
}
