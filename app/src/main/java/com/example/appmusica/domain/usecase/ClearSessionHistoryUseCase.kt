package com.example.appmusica.domain.usecase

import com.example.appmusica.data.local.dao.UserSessionDao
import javax.inject.Inject

class ClearSessionHistoryUseCase @Inject constructor(
    private val sessionDao: UserSessionDao
) {
    suspend operator fun invoke(userId: Long) = sessionDao.clearHistoryByUserId(userId)
}
