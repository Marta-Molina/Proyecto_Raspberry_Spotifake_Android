package com.example.appmusica.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.appmusica.domain.model.Cancion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("queue_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val QUEUE_KEY = "playback_queue"
        private const val CURRENT_INDEX_KEY = "current_index"
    }

    fun saveQueue(queue: List<Cancion>) {
        val json = gson.toJson(queue)
        prefs.edit().putString(QUEUE_KEY, json).apply()
    }

    fun getQueue(): List<Cancion> {
        val json = prefs.getString(QUEUE_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Cancion>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveCurrentIndex(index: Int) {
        prefs.edit().putInt(CURRENT_INDEX_KEY, index).apply()
    }

    fun getCurrentIndex(): Int {
        return prefs.getInt(CURRENT_INDEX_KEY, 0)
    }

    fun clearQueue() {
        prefs.edit().clear().apply()
    }
}
