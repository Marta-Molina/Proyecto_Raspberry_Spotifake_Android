package com.example.appmusica.data.repository

import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.data.local.dao.UserSessionDao
import com.example.appmusica.data.remote.request.UserRequest
import com.example.appmusica.domain.repository.AuthRepository
import com.example.appmusica.retrofit.ApiCancionesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService,
    private val authManager: AuthManager,
    private val sessionDao: UserSessionDao
) : AuthRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val response = api.login(UserRequest(correo = email, pass = password))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    authManager.saveToken(user.token ?: "")
                    authManager.saveUserId(user.id ?: -1L)
                    authManager.saveIsAdmin(user.admin ?: false)
                    authManager.saveUrlImagen(user.urlImagen)
                    authManager.saveUsername(user.username ?: "Usuario")
                    authManager.saveIsPremium(user.premium ?: false)
                    
                    recordSession(user.id ?: -1L, user.token ?: "", "Login")
                    
                    withContext(Dispatchers.Main) {
                        callback(true, null)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorMsg = if (response.code() == 401) "Correo o contraseña incorrectos" else "Error ${response.code()}: ${response.message()}"
                        callback(false, errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error de conexión: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun register(username: String, correo: String, password: String, callback: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val response = api.register(
                    UserRequest(
                        username = username,
                        correo = correo,
                        pass = password
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    authManager.saveToken(user.token ?: "")
                    authManager.saveUserId(user.id ?: -1L)
                    authManager.saveIsAdmin(user.admin ?: false)
                    authManager.saveUrlImagen(user.urlImagen)
                    authManager.saveUsername(user.username ?: "Usuario")
                    authManager.saveIsPremium(user.premium ?: false)

                    recordSession(user.id ?: -1L, user.token ?: "", "Register")

                    withContext(Dispatchers.Main) {
                        callback(true, null)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(false, "Registro fallido: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    private suspend fun recordSession(userId: Long, token: String, action: String) {
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sdfTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val now = java.util.Date()
        
        val session = com.example.appmusica.data.local.entities.UserSession(
            userId = userId,
            date = sdfDate.format(now),
            time = sdfTime.format(now),
            token = token,
            action = action
        )
        sessionDao.insertSession(session)
    }

    override fun logout() {
        val userId = authManager.getUserId()
        val token = authManager.getToken() ?: ""
        scope.launch {
            recordSession(userId, token, "Cerrar sesión")
            authManager.clear()
        }
    }

    override fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }
}
