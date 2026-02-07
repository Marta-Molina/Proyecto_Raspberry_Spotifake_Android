package com.example.appmusica.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Registered : AuthState()                  // Nuevo: usuario registrado pero no autenticado
    data class Authenticated(val message: String? = null) : AuthState() // Nuevo: sesión iniciada correctamente
    data class Error(val error: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun currentUser() = repository.currentUser()

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.signIn(email, password) { success, message ->
                if (success) {
                    _authState.postValue(AuthState.Authenticated())
                } else {
                    _authState.postValue(AuthState.Error(message ?: "Error desconocido"))
                }
            }
        }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.register(email, password) { success, message ->
                if (success) {
                    // Asegurar que la cuenta NO quede como sesión iniciada automáticamente
                    repository.signOut()
                    // Indicar que la cuenta se creó correctamente, pero no está autenticada
                    _authState.postValue(AuthState.Registered)
                } else {
                    _authState.postValue(AuthState.Error(message ?: "Error desconocido"))
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.Idle
    }
}
