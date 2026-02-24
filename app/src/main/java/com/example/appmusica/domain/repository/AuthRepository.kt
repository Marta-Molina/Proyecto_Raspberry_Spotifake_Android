package com.example.appmusica.domain.repository

interface AuthRepository {
    fun login(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    )

    fun register(
        username: String,
        correo: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    )

    fun logout()
    fun isLoggedIn(): Boolean
}
