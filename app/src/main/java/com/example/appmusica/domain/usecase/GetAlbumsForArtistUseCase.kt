package com.example.appmusica.domain.usecase

import com.example.appmusica.domain.model.Album
import com.example.appmusica.domain.repository.CancionRepository
import javax.inject.Inject

class GetAlbumsForArtistUseCase @Inject constructor(
    private val repository: CancionRepository
) {
    suspend operator fun invoke(artist: String): List<Album> = repository.getAlbumsByArtist(artist)
}
