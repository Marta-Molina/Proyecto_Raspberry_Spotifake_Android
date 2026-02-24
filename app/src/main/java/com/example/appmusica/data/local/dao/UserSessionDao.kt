package com.example.appmusica.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appmusica.data.local.entities.UserSession
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Insert
    suspend fun insertSession(session: UserSession)

    @Query("SELECT * FROM user_session WHERE userId = :userId ORDER BY id DESC")
    fun getSessionsByUserId(userId: Long): Flow<List<UserSession>>

    @Query("DELETE FROM user_session WHERE userId = :userId")
    suspend fun clearHistoryByUserId(userId: Long)
}
