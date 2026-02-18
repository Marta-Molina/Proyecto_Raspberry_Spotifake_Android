package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.repository.PlaylistRepository
import javax.inject.Inject

class DeletePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteLista(id)
}
