package com.example.appmusica.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.data.local.entities.UserSession
import com.example.appmusica.domain.usecase.ClearSessionHistoryUseCase
import com.example.appmusica.domain.usecase.GetSessionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getSessionHistoryUseCase: GetSessionHistoryUseCase,
    private val clearSessionHistoryUseCase: ClearSessionHistoryUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<UserSession>>(emptyList())
    val sessions: StateFlow<List<UserSession>> = _sessions

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val userId = authManager.getUserId()
        if (userId != -1L) {
            viewModelScope.launch {
                getSessionHistoryUseCase(userId).collectLatest {
                    _sessions.value = it
                }
            }
        }
    }

    fun clearHistory() {
        val userId = authManager.getUserId()
        if (userId != -1L) {
            viewModelScope.launch {
                clearSessionHistoryUseCase(userId)
            }
        }
    }
}
