package com.example.appmusica.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appmusica.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authResult = MutableLiveData<Pair<Boolean, String?>>()
    val authResult: LiveData<Pair<Boolean, String?>> = _authResult

    fun login(email: String, password: String) {
        authRepository.login(email, password) { success, message ->
            _authResult.postValue(success to message)
        }
    }

    fun register(username: String, email: String, password: String) {
        authRepository.register(username, email, password) { success, message ->
            _authResult.postValue(success to message)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}
