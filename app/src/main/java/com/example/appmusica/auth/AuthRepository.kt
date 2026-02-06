package com.example.appmusica.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun currentUser() = auth.currentUser

    fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(
                        false,
                        task.exception?.localizedMessage ?: "Error de autenticación"
                    )
                }
            }
    }

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(
                        false,
                        task.exception?.localizedMessage ?: "Error de registro"
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}
