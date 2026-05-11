package com.example.appmusica.presentation.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.domain.model.SolicitudAmistad
import com.example.appmusica.domain.repository.SocialRepository
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.retrofit.ApiCancionesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val api: ApiCancionesService
) : ViewModel() {

    private val _friends = MutableLiveData<List<UserResponse>>()
    val friends: LiveData<List<UserResponse>> = _friends

    private val _searchResults = MutableLiveData<List<UserResponse>>()
    val searchResults: LiveData<List<UserResponse>> = _searchResults

    private val _pendingRequests = MutableLiveData<List<Pair<SolicitudAmistad, UserResponse?>>>()
    val pendingRequests: LiveData<List<Pair<SolicitudAmistad, UserResponse?>>> = _pendingRequests

    fun loadSocialData() {
        viewModelScope.launch {
            try {
                // Load Friends
                val friendIds = socialRepository.getFriends()
                val friendsWithDetails = friendIds.mapNotNull { id ->
                    socialRepository.getUsuarioById(id)
                }
                _friends.value = friendsWithDetails
                
                // Load Pending Requests with Requester Details
                val requests = socialRepository.getPendingRequests()
                val requestsWithDetails = requests.map { req ->
                    val user = socialRepository.getUsuarioById(req.remitenteId)
                    req to user
                }
                _pendingRequests.value = requestsWithDetails
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val response = api.searchUsers(query)
                if (response.isSuccessful) {
                    _searchResults.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendFriendRequest(userId: Long) {
        viewModelScope.launch {
            try {
                socialRepository.sendFriendRequest(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptFriend(reqId: Int) {
        viewModelScope.launch {
            try {
                if (socialRepository.acceptFriendRequest(reqId)) {
                    loadSocialData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectFriend(reqId: Int) {
        viewModelScope.launch {
            try {
                if (socialRepository.rejectFriendRequest(reqId)) {
                    loadSocialData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
