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
) : ViewModel() {

    private val _canciones = MutableLiveData<List<Cancion>>()
    val canciones: LiveData<List<Cancion>> = _canciones

    // Lista de artistas derivada de las canciones cargadas (para mostrar en la UI)
    private val _artistas = MutableLiveData<List<com.example.appmusica.domain.model.Artista>>()
    val artistas: LiveData<List<com.example.appmusica.domain.model.Artista>> = _artistas

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
    }

    private fun loadGeneros() {
        viewModelScope.launch {
            _generos.value = getGenerosUseCase()
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
            // Derivar artistas desde la lista filtrada para la UI de cards
            deriveArtistasFrom(filtered)
        }
    }

    private fun deriveArtistasFrom(list: List<Cancion>) {
        val artistas = list
            .groupBy { it.artista }
            .map { entry ->
                val nombre = entry.key
                val portada = entry.value.mapNotNull { it.urlPortada }.firstOrNull()
                com.example.appmusica.domain.model.Artista(nombre, portada)
            }
        _artistas.value = artistas
    }

    /**
     * Devuelve los álbumes para un artista (derivados de las canciones cargadas).
     */
    fun getAlbumsForArtist(artistName: String): List<com.example.appmusica.domain.model.Album> {
        val source = fullList.ifEmpty { _canciones.value ?: emptyList() }
        return source
            .filter { it.artista.equals(artistName, ignoreCase = true) }
            .groupBy { it.album }
            .map { entry ->
                val nombre = entry.key
                val portada = entry.value.mapNotNull { it.urlPortada }.firstOrNull()
                com.example.appmusica.domain.model.Album(nombre, artistName, portada)
            }
    }

    /**
     * Devuelve las canciones de un álbum de un artista.
     */
    fun getCancionesForAlbum(artistName: String, albumName: String): List<Cancion> {
        val source = fullList.ifEmpty { _canciones.value ?: emptyList() }
        return source.filter { it.artista.equals(artistName, true) && it.album.equals(albumName, true) }
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
            updateCancionUseCase(cancion.id, updatedCancion)
            fullList = emptyList()
            loadCanciones()
        }
    }

    fun removeLike(cancion: Cancion) {
        viewModelScope.launch {
            val updatedCancion = cancion.copy(likes = maxOf(0, cancion.likes - 1))
            updateCancionUseCase(cancion.id, updatedCancion)
            fullList = emptyList()
            loadCanciones()
        }
    }

    fun toggleLike(cancion: Cancion) {
        viewModelScope.launch {
            // Decidimos que por ahora el like solo incremente para asegurar que se vea el cambio
            val updatedCancion = cancion.copy(likes = cancion.likes + 1)
            updateCancionUseCase(cancion.id, updatedCancion)
            loadCanciones()
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
