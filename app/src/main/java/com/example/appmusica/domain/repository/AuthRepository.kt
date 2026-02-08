package com.example.appmusica.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    )

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    )

    fun logout()
}
