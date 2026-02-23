package com.example.appmusica.data.repository

import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.data.remote.request.UserRequest
import com.example.appmusica.domain.repository.AuthRepository
import com.example.appmusica.retrofit.ApiCancionesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiCancionesService,
    private val authManager: AuthManager
) : AuthRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val response = apiService.login(UserRequest(correo = email, pass = password))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    authManager.saveToken(user.token)
                    authManager.saveUserId(user.id)
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
                val response = apiService.register(
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
                    authManager.saveToken(user.token)
                    authManager.saveUserId(user.id)
                    callback(true, null)
                } else {
                    callback(false, "Registro fallido: ${response.message()}")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    override fun logout() {
        authManager.clear()
    }

    override fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }
}
