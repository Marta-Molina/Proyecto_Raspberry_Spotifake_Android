package com.example.appmusica.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Long,
    val date: String,    // Format: YYYY-MM-DD
    val time: String,    // Format: HH:mm:ss
    val token: String,
    val action: String   // "Login" or "Register"
)
