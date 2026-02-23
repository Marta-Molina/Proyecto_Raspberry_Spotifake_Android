package com.example.appmusica.presentation.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.model.Playlist
import com.example.appmusica.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val addCancionToPlaylistUseCase: AddCancionToPlaylistUseCase,
    private val removeCancionFromPlaylistUseCase: RemoveCancionFromPlaylistUseCase,
    private val getPlaylistCancionesUseCase: GetPlaylistCancionesUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase
) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _playlistSongs = MutableLiveData<List<Cancion>>()
    val playlistSongs: LiveData<List<Cancion>> = _playlistSongs

    fun loadUserPlaylists(userId: Int) {
        viewModelScope.launch {
            _playlists.value = getUserPlaylistsUseCase(userId)
        }
    }

    fun loadAllPlaylists() {
        viewModelScope.launch {
            _playlists.value = getPlaylistsUseCase()
        }
    }

    fun loadPlaylists(userId: Int) {
        loadUserPlaylists(userId)
    }

    private suspend fun getUserListas(userId: Int): List<Playlist> {
        // Aprovechamos que tenemos el caso de uso aunque en el repo se llame diferente
        return getPlaylistsUseCase() // Verificaremos si el caso de uso es correcto o necetamos inyectar el de usuario
    }

    fun createPlaylist(nombre: String, userId: Int) { 
        viewModelScope.launch {
            val result = createPlaylistUseCase(Playlist(nombre = nombre, idUsuario = userId))
            if (result != null) {
                loadUserPlaylists(userId)
            }
        }
    }

    fun deletePlaylist(id: Int, userId: Int? = null) {
        android.util.Log.d("PlaylistVM", "Deleting playlist $id for user $userId")
        viewModelScope.launch {
            deletePlaylistUseCase(id)
            if (userId != null) loadUserPlaylists(userId) else loadAllPlaylists()
        }
    }

    fun updatePlaylist(id: Int, nombre: String, userId: Int) {
        android.util.Log.d("PlaylistVM", "Updating playlist $id to $nombre")
        viewModelScope.launch {
            updatePlaylistUseCase(id, Playlist(id = id, nombre = nombre, idUsuario = userId))
            loadUserPlaylists(userId)
        }
    }

    fun loadPlaylistSongs(playlistId: Int) {
        viewModelScope.launch {
            _playlistSongs.value = getPlaylistCancionesUseCase(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Int, cancionId: Int) {
        viewModelScope.launch {
            addCancionToPlaylistUseCase(playlistId, cancionId)
            loadPlaylistSongs(playlistId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, cancionId: Int) {
        android.util.Log.d("PlaylistVM", "Removing song $cancionId from playlist $playlistId")
        viewModelScope.launch {
            removeCancionFromPlaylistUseCase(playlistId, cancionId)
            loadPlaylistSongs(playlistId)
        }
    }
}
