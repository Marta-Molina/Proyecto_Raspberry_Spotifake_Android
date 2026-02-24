package com.example.appmusica.presentation.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.appmusica.R
import com.example.appmusica.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val tilUsername = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilUsername)
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

            Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
            authViewModel.login(user, pass)
        }

        btnRegister.setOnClickListener {
            if (tilUsername.visibility == android.view.View.GONE) {
                tilUsername.visibility = android.view.View.VISIBLE
                btnRegister.text = "Confirmar registro"
                btnLogin.visibility = android.view.View.GONE
                return@setOnClickListener
            }

            val username = etUsername.text.toString().trim()
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString()

            if (username.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show()
            authViewModel.register(username, user, pass)
        }

        authViewModel.authResult.observe(this) { result ->
            val success = result.first
            val message = result.second

            if (success) {
                // Login correcto → vamos a MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this,
                    message ?: "Error desconocido",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
