package com.example.appmusica.presentation.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmusica.domain.model.Notificacion
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

    private val _sentRequests = MutableLiveData<List<Pair<SolicitudAmistad, UserResponse?>>>()
    val sentRequests: LiveData<List<Pair<SolicitudAmistad, UserResponse?>>> = _sentRequests

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _notifications = MutableLiveData<List<Notificacion>>()
    val notifications: LiveData<List<Notificacion>> = _notifications

    private val _sharedPlaylists = MutableLiveData<List<com.example.appmusica.domain.model.Playlist>>()
    val sharedPlaylists: LiveData<List<com.example.appmusica.domain.model.Playlist>> = _sharedPlaylists

    private val sentRequestIds = mutableSetOf<Long>()

    fun loadSocialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load Notifications
                _notifications.value = socialRepository.getNotifications()

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

                // Load Sent Requests
                val sent = socialRepository.getSentRequests()
                val sentWithDetails = sent.map { req ->
                    val user = socialRepository.getUsuarioById(req.destinatarioId)
                    req to user
                }
                _sentRequests.value = sentWithDetails

                // Update sentRequestIds to maintain UI consistency
                sentRequestIds.clear()
                sent.forEach { sentRequestIds.add(it.destinatarioId) }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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
        sentRequestIds.add(userId)
        // Actualizar resultados de búsqueda si existen para reflejar el estado inmediatamente
        _searchResults.value = _searchResults.value

        viewModelScope.launch {
            try {
                socialRepository.sendFriendRequest(userId)
            } catch (e: Exception) {
                sentRequestIds.remove(userId)
                _searchResults.value = _searchResults.value
                e.printStackTrace()
            }
        }
    }

    fun isRequestSent(userId: Long): Boolean = sentRequestIds.contains(userId)

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

    fun deleteFriend(friendId: Long) {
        viewModelScope.launch {
            try {
                if (socialRepository.deleteFriend(friendId)) {
                    loadSocialData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            try {
                if (socialRepository.clearNotifications()) {
                    _notifications.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sharePlaylist(playlistId: Long, userId: Long) {
        viewModelScope.launch {
            try {
                socialRepository.sharePlaylist(playlistId, userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSharedPlaylists(friendId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val playlistIds = socialRepository.getSharedPlaylists(friendId)
                val playlists = playlistIds.mapNotNull { id ->
                    socialRepository.getPlaylistById(id)
                }
                _sharedPlaylists.value = playlists
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            try {
                socialRepository.markNotificationAsRead(id)
                loadSocialData() // Refresh
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
