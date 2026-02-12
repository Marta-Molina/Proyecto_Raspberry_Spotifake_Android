package com.example.appmusica.retrofit

data class LoginResponse(
    val token : String,
    val userId: Int,
    val email: String
)
