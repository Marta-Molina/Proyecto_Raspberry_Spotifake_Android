package com.example.appmusica.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    // Lazily obtain FirebaseAuth to avoid throwing during construction when Firebase
    // isn't configured (no google-services.json / plugin applied). Methods handle
    // the uninitialized case and return failures via the callbacks.
    private fun getAuth(): FirebaseAuth? {
        return try {
            // Ensure FirebaseApp is initialized; if initializeApp returns null, getInstance()
            // may still throw. We catch IllegalStateException below.
            if (FirebaseApp.getApps().isEmpty()) {
                // try to initialize; this will return null if no default options are available
                FirebaseApp.initializeApp(/* context not available here - rely on automatic init */)
            }
            FirebaseAuth.getInstance()
        } catch (e: IllegalStateException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun currentUser() = try {
        getAuth()?.currentUser
    } catch (e: Exception) {
        null
    }

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val auth = getAuth()
        if (auth == null) {
            callback(false, "Firebase no configurado. Añade google-services.json y aplica el plugin de Google Services.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage ?: "Error de autenticación")
                }
            }
    }

    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val auth = getAuth()
        if (auth == null) {
            callback(false, "Firebase no configurado. Añade google-services.json y aplica el plugin de Google Services.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage ?: "Error de registro")
                }
            }
    }

    fun signOut() {
        try {
            getAuth()?.signOut()
        } catch (e: Exception) {
            // ignore
        }
    }
}

