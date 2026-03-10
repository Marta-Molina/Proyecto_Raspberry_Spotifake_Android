package com.example.appmusica.presentation.canciones.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.model.Genero
import com.example.appmusica.domain.usecase.GetCancionesUseCase
import com.example.appmusica.domain.usecase.AddCancionUseCase
import com.example.appmusica.domain.usecase.DeleteCancionUseCase
import com.example.appmusica.domain.usecase.UpdateCancionUseCase
import com.example.appmusica.domain.usecase.GetCancionUseCase
import com.example.appmusica.domain.usecase.GetGenerosUseCase
import com.example.appmusica.domain.usecase.GetArtistasUseCase
import com.example.appmusica.domain.usecase.GetAlbumsForArtistUseCase
import com.example.appmusica.domain.usecase.GetCancionesForAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CancionesViewModel @Inject constructor(
    private val getCancionesUseCase: GetCancionesUseCase,
    private val addCancionUseCase: AddCancionUseCase,
    private val deleteCancionUseCase: DeleteCancionUseCase,
    private val updateCancionUseCase: UpdateCancionUseCase,
    private val getCancionUseCase: GetCancionUseCase,
    private val getGenerosUseCase: GetGenerosUseCase
    ,
    private val getArtistasUseCase: GetArtistasUseCase,
    private val getAlbumsForArtistUseCase: GetAlbumsForArtistUseCase,
    private val getCancionesForAlbumUseCase: GetCancionesForAlbumUseCase,
    private val cancionRepository: com.example.appmusica.domain.repository.CancionRepository,
    private val artistaRepository: com.example.appmusica.domain.repository.ArtistaRepository
) : ViewModel() {

    private val _canciones = MutableLiveData<List<Cancion>>()
    val canciones: LiveData<List<Cancion>> = _canciones

    // Lista de artistas derivada de las canciones cargadas (para mostrar en la UI)
    private val _artistas = MutableLiveData<List<com.example.appmusica.domain.model.Artista>>()
    val artistas: LiveData<List<com.example.appmusica.domain.model.Artista>> = _artistas

    private val _albums = MutableLiveData<List<com.example.appmusica.domain.model.Album>>()
    val albums: LiveData<List<com.example.appmusica.domain.model.Album>> = _albums

    private val _albumSongs = MutableLiveData<List<Cancion>>()
    val albumSongs: LiveData<List<Cancion>> = _albumSongs

    private val _currentAlbum = MutableLiveData<com.example.appmusica.domain.model.Album?>()
    val currentAlbum: LiveData<com.example.appmusica.domain.model.Album?> = _currentAlbum

    // Resultado de la última operación de borrado: true=ok, false=error, null=no hay evento
    private val _deleteResult = MutableLiveData<Boolean?>(null)
    val deleteResult: LiveData<Boolean?> = _deleteResult

    private val _generos = MutableLiveData<List<Genero>>()
    val generos: LiveData<List<Genero>> = _generos

    private val _selectedCancion = MutableLiveData<Cancion?>()
    val selectedCancion: LiveData<Cancion?> = _selectedCancion

    private val _currentArtista = MutableLiveData<com.example.appmusica.domain.model.Artista?>()
    val currentArtista: LiveData<com.example.appmusica.domain.model.Artista?> = _currentArtista

    private val _popularSongs = MutableLiveData<List<Cancion>>()
    val popularSongs: LiveData<List<Cancion>> = _popularSongs

    private var fullList: List<Cancion> = emptyList()
    private var currentQuery: String? = null
    private var selectedGeneroId: Int? = null

    init {
        loadGeneros()
        loadCanciones()
        loadArtistas()
    }

    private fun loadGeneros() {
        viewModelScope.launch {
            _generos.value = getGenerosUseCase()
        }
    }

    private fun loadArtistas() {
        viewModelScope.launch {
            _artistas.value = getArtistasUseCase()
        }
    }

    fun loadCanciones(query: String? = currentQuery, generoId: Int? = selectedGeneroId) {
        currentQuery = query
        selectedGeneroId = if (generoId == 0) null else generoId

        viewModelScope.launch {
            if (fullList.isEmpty()) {
                fullList = getCancionesUseCase()
            }
            
            var filtered = fullList

            // Filtro por texto
            if (!currentQuery.isNullOrBlank()) {
                filtered = filtered.filter { cancion ->
                    cancion.nombre.contains(currentQuery!!, ignoreCase = true) ||
                    cancion.artistas?.any { it.contains(currentQuery!!, ignoreCase = true) } == true ||
                    cancion.albumes?.any { it.contains(currentQuery!!, ignoreCase = true) } == true
                }
            }

            // Filtro por género
            if (selectedGeneroId != null) {
                filtered = filtered.filter { it.genero == selectedGeneroId }
            }

            _canciones.value = filtered
        }
    }
    /**
     * Carga los álbumes de un artista desde la API y publica en LiveData.
     */
    fun loadAlbumsForArtist(artistId: Int) {
        viewModelScope.launch {
            _albums.value = getAlbumsForArtistUseCase(artistId)
        }
    }

    /**
     * Carga las canciones para un álbum de un artista desde la API y publica en LiveData.
     */
    fun loadCancionesForAlbum(albumId: Int) {
        viewModelScope.launch {
            _currentAlbum.value = _albums.value?.find { it.id == albumId }
            _albumSongs.value = getCancionesForAlbumUseCase(albumId)
        }
    }

    fun loadArtistaDetalle(artistId: Int) {
        viewModelScope.launch {
            // Cargar el artista directamente del repo para tener datos frescos (seguidores, likes)
            val artista = artistaRepository.getArtistaById(artistId)
            _currentArtista.value = artista

            // Cargar canciones "populares" del artista (ordenar por reproducciones)
            if (fullList.isEmpty()) fullList = getCancionesUseCase()
            val artistSongs = fullList.filter { it.artistasIds?.contains(artistId) == true }
            _popularSongs.value = artistSongs.sortedByDescending { it.reproducciones }.take(5)
            
            // Cargar álbumes del artista
            _albums.value = getAlbumsForArtistUseCase(artistId)
        }
    }

    fun followArtista(id: Int) {
        viewModelScope.launch {
            val success = artistaRepository.followArtista(id)
            if (success) {
                // Refresh artist info
                val updated = artistaRepository.getArtistaById(id)
                _currentArtista.value = updated
            }
        }
    }

    fun unfollowArtista(id: Int) {
        viewModelScope.launch {
            val success = artistaRepository.unfollowArtista(id)
            if (success) {
                // Refresh artist info
                val updated = artistaRepository.getArtistaById(id)
                _currentArtista.value = updated
            }
        }
    }

    fun incrementReproducciones(id: Int) {
        viewModelScope.launch {
            cancionRepository.incrementReproducciones(id)
        }
    }

    fun addCancion(cancion: Cancion) {
        viewModelScope.launch {
            addCancionUseCase(cancion)
            fullList = emptyList()
            loadCanciones()
        }
    }

    fun deleteCancion(id: Int) {
        viewModelScope.launch {
            val success = deleteCancionUseCase(id)
            if (success) {
                fullList = emptyList()
                loadCanciones()
                _deleteResult.value = true
            } else {
                _deleteResult.value = false
            }
        }
    }

    /**
     * Resetea el evento de resultado de borrado. Llamar desde la UI después de procesarlo.
     */
    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    fun updateCancion(id: Int, cancion: Cancion) {
        viewModelScope.launch {
            updateCancionUseCase(id, cancion)
            fullList = emptyList()
            loadCanciones()
        }
    }

    fun addLike(cancion: Cancion) {
        viewModelScope.launch {
            val updatedCancion = cancion.copy(likes = cancion.likes + 1)
            updateLocalSongState(updatedCancion)
            cancionRepository.likeCancion(cancion.id)
        }
    }

    fun removeLike(cancion: Cancion) {
        viewModelScope.launch {
            val updatedCancion = cancion.copy(likes = maxOf(0, cancion.likes - 1))
            updateLocalSongState(updatedCancion)
            cancionRepository.unlikeCancion(cancion.id)
        }
    }

    fun toggleLike(cancion: Cancion, isCurrentlyLiked: Boolean) {
        if (isCurrentlyLiked) {
            removeLike(cancion)
        } else {
            addLike(cancion)
        }
    }

    private fun updateLocalSongState(updatedCancion: Cancion) {
        // Update in main list
        val currentList = _canciones.value?.toMutableList() ?: mutableListOf()
        val idx = currentList.indexOfFirst { it.id == updatedCancion.id }
        if (idx != -1) {
            currentList[idx] = updatedCancion
            _canciones.value = currentList
        }

        // Update in album songs list if active
        val currentAlbumList = _albumSongs.value?.toMutableList() ?: mutableListOf()
        val albumIdx = currentAlbumList.indexOfFirst { it.id == updatedCancion.id }
        if (albumIdx != -1) {
            currentAlbumList[albumIdx] = updatedCancion
            _albumSongs.value = currentAlbumList
        }

        // Update selected song (Detail screen)
        if (_selectedCancion.value?.id == updatedCancion.id) {
            _selectedCancion.value = updatedCancion
        }
    }

    fun selectCancion(position: Int) {
        viewModelScope.launch {
            val list = _canciones.value
            val cancion = if (list != null && position < list.size) {
                list[position]
            } else {
                null
            }
            _selectedCancion.value = cancion
        }
    }

    fun setCanciones(lista: List<Cancion>) {
        _canciones.value = lista
    }

    fun getCancion(position: Int): Cancion? {
        val list = _canciones.value
        return if (list != null && position < list.size) list[position] else null
    }

}
