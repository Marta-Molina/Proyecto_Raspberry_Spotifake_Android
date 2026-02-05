package com.example.appmusica

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppMusicaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase. Ensure google-services.json is present in app/ for full config.
        FirebaseApp.initializeApp(this)
    }
}
