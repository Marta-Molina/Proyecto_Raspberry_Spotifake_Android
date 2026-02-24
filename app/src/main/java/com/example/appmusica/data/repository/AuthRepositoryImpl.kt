package com.example.appmusica.data.repository

import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.data.remote.request.UserRequest
import com.example.appmusica.domain.repository.AuthRepository
import com.example.appmusica.retrofit.ApiCancionesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                    authManager.saveUserId(user.id)
                    authManager.saveIsAdmin(user.admin)
                    authManager.saveUrlImagen(user.urlImagen)
                    
                    recordSession(user.id, user.token ?: "", "Login")
                    
                    callback(true, null)
                } else {
                    callback(false, "Login fallido: ${response.message()}")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    override fun register(nombre: String, correo: String, password: String, callback: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val response = api.register(
                    UserRequest(
                        nombre = nombre,
                        correo = correo,
                        pass = password,
                        apellido1 = "",
                        apellido2 = "",
                        admin = false,
                        premium = false
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    authManager.saveToken(user.token ?: "")
                    authManager.saveUserId(user.id)
                    authManager.saveIsAdmin(user.admin)
                    authManager.saveUrlImagen(user.urlImagen)

                    recordSession(user.id, user.token ?: "", "Register")

                    callback(true, null)
                } else {
                    callback(false, "Registro fallido: ${response.message()}")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.localizedMessage}")
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
        authManager.clear()
    }

    override fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }
}
