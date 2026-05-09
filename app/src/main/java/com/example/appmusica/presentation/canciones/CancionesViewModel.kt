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
    private val artistaRepository: com.example.appmusica.domain.repository.ArtistaRepository,
    private val socialRepository: com.example.appmusica.domain.repository.SocialRepository,
    private val mascotaRepository: com.example.appmusica.domain.repository.MascotaRepository,
    private val queueManager: com.example.appmusica.data.local.QueueManager
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

    private val _lyrics = MutableLiveData<com.example.appmusica.domain.model.Letra?>()
    val lyrics: LiveData<com.example.appmusica.domain.model.Letra?> = _lyrics

    private val _activeMascota = MutableLiveData<com.example.appmusica.domain.model.Mascota?>()
    val activeMascota: LiveData<com.example.appmusica.domain.model.Mascota?> = _activeMascota

    private val _playbackQueue = MutableLiveData<List<Cancion>>(emptyList())
    val playbackQueue: LiveData<List<Cancion>> = _playbackQueue

    private val _stats = MutableLiveData<com.example.appmusica.domain.model.ResumenAnual?>()
    val stats: LiveData<com.example.appmusica.domain.model.ResumenAnual?> = _stats

    private var fullList: List<Cancion> = emptyList()
    private var currentQuery: String? = null
    private var selectedGeneroId: Int? = null

    init {
        loadGeneros()
        loadCanciones()
        loadArtistas()
        loadActiveMascota()
        loadSavedQueue()
    }

    private fun loadSavedQueue() {
        _playbackQueue.value = queueManager.getQueue()
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
                    cancion.artista?.contains(currentQuery!!, ignoreCase = true) == true ||
                    cancion.album?.contains(currentQuery!!, ignoreCase = true) == true
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
            // Cargar canciones del artista
            if (fullList.isEmpty()) fullList = getCancionesUseCase()
            val artistSongs = fullList.filter { it.artistaIds.contains(artistId) }
            _popularSongs.value = artistSongs.sortedByDescending { it.reproducciones }.take(5)

            // Calcular likes totales a partir de las canciones para asegurar que la suma sea correcta
            val totalLikes = artistSongs.sumOf { it.likes }

            // Cargar el artista directamente del repo
            val artista = artistaRepository.getArtistaById(artistId)
            _currentArtista.value = artista?.copy(likesTotales = totalLikes)
            
            // Cargar álbumes del artista
            _albums.value = getAlbumsForArtistUseCase(artistId)
        }
    }

    fun followArtista(id: Int) {
        val current = _currentArtista.value ?: return
        // Optimistic update
        _currentArtista.value = current.copy(
            siguiendo = true,
            seguidores = current.seguidores + 1
        )
        
        viewModelScope.launch {
            val success = socialRepository.followArtista(id)
            if (success) {
                artistaRepository.incrementFollowers(id)
                // Refresh to confirm final state from server
                val updated = artistaRepository.getArtistaById(id)
                if (updated != null) {
                    _currentArtista.value = updated
                }
            } else {
                // Rollback on failure
                _currentArtista.value = current
            }
        }
    }

    fun unfollowArtista(id: Int) {
        val current = _currentArtista.value ?: return
        // Optimistic update
        _currentArtista.value = current.copy(
            siguiendo = false,
            seguidores = maxOf(0, current.seguidores - 1)
        )
        
        viewModelScope.launch {
            val success = socialRepository.unfollowArtista(id)
            if (success) {
                artistaRepository.decrementFollowers(id)
                // Refresh to confirm final state from server
                val updated = artistaRepository.getArtistaById(id)
                if (updated != null) {
                    _currentArtista.value = updated
                }
            } else {
                // Rollback on failure
                _currentArtista.value = current
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
            // Incrementar contador global en la tabla de canciones
            cancionRepository.likeCancion(cancion.id)
            // Registrar el like social del usuario
            val success = socialRepository.likeCancion(cancion.id)
            if (success) {
                val updatedCancion = cancion.copy(likes = cancion.likes + 1)
                updateLocalSongState(updatedCancion)
            }
        }
    }

    fun removeLike(cancion: Cancion) {
        viewModelScope.launch {
            // Decrementar contador global
            cancionRepository.unlikeCancion(cancion.id)
            // Quitar el like social
            val success = socialRepository.unlikeCancion(cancion.id)
            if (success) {
                val updatedCancion = cancion.copy(likes = maxOf(0, cancion.likes - 1))
                updateLocalSongState(updatedCancion)
            }
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

    fun loadLyrics(cancionId: Int) {
        viewModelScope.launch {
            _lyrics.value = socialRepository.getLyrics(cancionId)
        }
    }

    fun getStats(year: Int) {
        viewModelScope.launch {
            _stats.value = cancionRepository.getStats(year)
        }
    }

    fun loadActiveMascota() {
        viewModelScope.launch {
            _activeMascota.value = mascotaRepository.getActiveMascota()
        }
    }

    // --- Playback Queue Logic ---
    
    fun addToQueue(cancion: Cancion) {
        val currentQueue = _playbackQueue.value?.toMutableList() ?: mutableListOf()
        currentQueue.add(cancion)
        _playbackQueue.value = currentQueue
        queueManager.saveQueue(currentQueue)
    }

    fun playNext(cancion: Cancion) {
        val currentQueue = _playbackQueue.value?.toMutableList() ?: mutableListOf()
        // Insertar al principio de la cola para que sea lo siguiente en sonar
        currentQueue.add(0, cancion)
        _playbackQueue.value = currentQueue
        queueManager.saveQueue(currentQueue)
    }

    fun clearQueue() {
        _playbackQueue.value = emptyList()
        queueManager.clearQueue()
    }

    fun removeFromQueue(position: Int) {
        val currentQueue = _playbackQueue.value?.toMutableList() ?: return
        if (position >= 0 && position < currentQueue.size) {
            currentQueue.removeAt(position)
            _playbackQueue.value = currentQueue
            queueManager.saveQueue(currentQueue)
        }
    }
}
