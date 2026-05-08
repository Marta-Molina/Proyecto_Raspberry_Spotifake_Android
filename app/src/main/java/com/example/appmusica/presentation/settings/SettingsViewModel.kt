package com.example.appmusica.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.domain.model.Mascota
import com.example.appmusica.domain.repository.MascotaRepository
import com.example.appmusica.domain.repository.AlarmaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mascotaRepository: MascotaRepository,
    private val alarmaRepository: AlarmaRepository
) : ViewModel() {

    private val _alarms = MutableLiveData<List<com.example.appmusica.domain.model.Alarma>>()
    val alarms: LiveData<List<com.example.appmusica.domain.model.Alarma>> = _alarms

    private val _mascotas = MutableLiveData<List<Mascota>>()
    val mascotas: LiveData<List<Mascota>> = _mascotas

    private val _activeMascota = MutableLiveData<Mascota?>()
    val activeMascota: LiveData<Mascota?> = _activeMascota

    fun loadMascotas() {
        viewModelScope.launch {
            _mascotas.value = mascotaRepository.getUserMascotas()
            _activeMascota.value = mascotaRepository.getActiveMascota()
        }
    }

    fun loadAlarms() {
        viewModelScope.launch {
            _alarms.value = alarmaRepository.getAlarms()
        }
    }

    fun createAlarm(alarm: com.example.appmusica.domain.model.Alarma) {
        viewModelScope.launch {
            val result = alarmaRepository.createAlarm(alarm)
            if (result != null) {
                loadAlarms()
            }
        }
    }

    fun updateAlarm(id: Int, alarm: com.example.appmusica.domain.model.Alarma) {
        viewModelScope.launch {
            if (alarmaRepository.updateAlarm(id, alarm)) {
                loadAlarms()
            }
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            if (alarmaRepository.deleteAlarm(id)) {
                loadAlarms()
            }
        }
    }

    fun selectMascota(mascota: Mascota) {
        viewModelScope.launch {
            val success = mascotaRepository.setActiveMascota(mascota.id)
            if (success) {
                loadMascotas()
            }
        }
    }

    fun buyMascota(mascotaId: Int) {
        viewModelScope.launch {
            if (mascotaRepository.buyMascota(mascotaId)) {
                loadMascotas()
            }
        }
    }
}
