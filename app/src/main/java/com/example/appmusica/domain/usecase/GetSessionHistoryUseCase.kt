package com.example.appmusica.domain.usecase

import com.example.appmusica.data.local.dao.UserSessionDao
import com.example.appmusica.data.local.entities.UserSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val sessionDao: UserSessionDao
) {
    operator fun invoke(userId: Long): Flow<List<UserSession>> = sessionDao.getSessionsByUserId(userId)
}
