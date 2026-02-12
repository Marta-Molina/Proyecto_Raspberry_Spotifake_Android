package com.example.appmusica

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppMusicaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val app = FirebaseApp.initializeApp(this)
        android.util.Log.d("FIREBASE_TEST", "Firebase init = $app")
    }

}
