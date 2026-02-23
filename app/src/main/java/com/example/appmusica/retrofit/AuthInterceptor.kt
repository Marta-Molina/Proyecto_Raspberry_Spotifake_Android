package com.example.appmusica.retrofit

import com.example.appmusica.data.local.AuthManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                authManager.getToken()?.let {
                    addHeader("Authorization", "Bearer $it")
                }
                addHeader("ngrok-skip-browser-warning", "true")
            }
            .build()
        return chain.proceed(request)
    }
}
