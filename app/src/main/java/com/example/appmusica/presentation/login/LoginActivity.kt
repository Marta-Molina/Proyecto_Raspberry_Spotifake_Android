package com.example.appmusica.presentation.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.appmusica.MainActivity
import com.example.appmusica.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUser = findViewById<EditText>(R.id.etUser)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString()
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.signIn(user, pass)
        }

        btnRegister.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString()
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.register(user, pass)
        }

        authViewModel.authState.observe(this, Observer { state ->
            when (state) {
                is AuthState.Loading -> Toast.makeText(this, "Procesando...", Toast.LENGTH_SHORT)
                    .show()

                is AuthState.Authenticated -> {
                    // Navegar a MainActivity sólo cuando la sesión esté iniciada
                    val email = authViewModel.currentUser()?.email ?: ""
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("user", email)
                    startActivity(intent)
                    finish()
                }

                is AuthState.Registered -> {
                    // Usuario registrado correctamente pero sin iniciar sesión
                    Toast.makeText(
                        this,
                        "Registro correcto. Por favor, inicia sesión.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AuthState.Error -> Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                is AuthState.Idle -> { /* no-op */
                }
            }
        })
    }
}