package com.example.appmusica.presentation.canciones.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.usecase.*
import com.example.appmusica.presentation.canciones.edit.EditCancionActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CancionesViewModel @Inject constructor(
    private val getCanciones: GetCancionesUseCase,
    private val addCancionUseCase: AddCancionUseCase,
    private val deleteCancionUseCase: DeleteCancionUseCase,
    private val updateCancionUseCase: UpdateCancionUseCase,
    private val getCancionUseCase: GetCancionUseCase
) : ViewModel() {

    private val _canciones = MutableLiveData<List<Cancion>>()
    val canciones: LiveData<List<Cancion>> = _canciones

    fun loadCanciones() {
        _canciones.value = getCanciones()
    }

    fun addCancion(cancion: Cancion) {
        addCancionUseCase(cancion)
        loadCanciones()
    }

    fun deleteCancion(position: Int) {
        deleteCancionUseCase(position)
        loadCanciones()
    }

    fun updateCancion(position: Int, cancion: Cancion) {
        updateCancionUseCase(position, cancion)
        loadCanciones()
    }

    fun getCancion(position: Int): Cancion {
        return getCancionUseCase(position)
    }

    fun editCancion(context: Context, position: Int) {
        val intent = Intent(context, EditCancionActivity::class.java)
        intent.putExtra("pos", position)
        context.startActivity(intent)
    }

}
