package com.example.appmusica.presentation.canciones.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CancionesViewModel @Inject constructor(
    private val getCancionesUseCase: GetCancionesUseCase,
    private val addCancionUseCase: AddCancionUseCase,
    private val deleteCancionUseCase: DeleteCancionUseCase,
    private val updateCancionUseCase: UpdateCancionUseCase,
    private val getCancionUseCase: GetCancionUseCase
) : ViewModel() {

    private val _canciones = MutableLiveData<List<Cancion>>()
    val canciones: LiveData<List<Cancion>> = _canciones

    private val _selectedCancion = MutableLiveData<Cancion?>()
    val selectedCancion: LiveData<Cancion?> = _selectedCancion

    fun loadCanciones(query: String? = null) {
        viewModelScope.launch {
            // Buscamos por nombre, artista o album usando el mismo query para simplificar en la UI
            _canciones.value = getCancionesUseCase(nombre = query, artista = query, album = query)
        }
    }

    fun addCancion(cancion: Cancion) {
        viewModelScope.launch {
            addCancionUseCase(cancion)
            loadCanciones()
        }
    }

    fun deleteCancion(id: Int) {
        viewModelScope.launch {
            deleteCancionUseCase(id)
            loadCanciones()
        }
    }

    fun updateCancion(id: Int, cancion: Cancion) {
        viewModelScope.launch {
            updateCancionUseCase(id, cancion)
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

    fun getCancion(position: Int): Cancion? {
        val list = _canciones.value
        return if (list != null && position < list.size) list[position] else null
    }

}
