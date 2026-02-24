package com.example.appmusica.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appmusica.data.local.dao.UserSessionDao
import com.example.appmusica.data.local.entities.UserSession

@Database(entities = [UserSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
}
