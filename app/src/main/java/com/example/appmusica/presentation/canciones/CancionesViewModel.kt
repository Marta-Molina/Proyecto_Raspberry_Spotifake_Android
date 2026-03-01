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
    private val cancionRepository: com.example.appmusica.domain.repository.CancionRepository
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

    // Resultado de la última operación de borrado: true=ok, false=error, null=no hay evento
    private val _deleteResult = MutableLiveData<Boolean?>(null)
    val deleteResult: LiveData<Boolean?> = _deleteResult

    private val _generos = MutableLiveData<List<Genero>>()
    val generos: LiveData<List<Genero>> = _generos

    private val _selectedCancion = MutableLiveData<Cancion?>()
    val selectedCancion: LiveData<Cancion?> = _selectedCancion

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
                    cancion.artista.contains(currentQuery!!, ignoreCase = true) ||
                    cancion.album.contains(currentQuery!!, ignoreCase = true)
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
            _albumSongs.value = getCancionesForAlbumUseCase(albumId)
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
            // Optimistically update local UI immediately
            val currentList = _canciones.value?.toMutableList() ?: mutableListOf()
            val idx = currentList.indexOfFirst { it.id == cancion.id }
            if (idx != -1) {
                currentList[idx] = cancion.copy(likes = cancion.likes + 1)
                _canciones.value = currentList
            }
            // Call dedicated public likes endpoint (not admin-only)
            cancionRepository.likeCancion(cancion.id)
        }
    }

    fun removeLike(cancion: Cancion) {
        viewModelScope.launch {
            // Optimistically update local UI immediately
            val currentList = _canciones.value?.toMutableList() ?: mutableListOf()
            val idx = currentList.indexOfFirst { it.id == cancion.id }
            if (idx != -1) {
                currentList[idx] = cancion.copy(likes = maxOf(0, cancion.likes - 1))
                _canciones.value = currentList
            }
            // Call dedicated public unlike endpoint
            cancionRepository.unlikeCancion(cancion.id)
        }
    }

    fun toggleLike(cancion: Cancion) {
        viewModelScope.launch {
            // Optimistically update local UI immediately
            val currentList = _canciones.value?.toMutableList() ?: mutableListOf()
            val idx = currentList.indexOfFirst { it.id == cancion.id }
            if (idx != -1) {
                currentList[idx] = cancion.copy(likes = cancion.likes + 1)
                _canciones.value = currentList
            }
            cancionRepository.likeCancion(cancion.id)
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
