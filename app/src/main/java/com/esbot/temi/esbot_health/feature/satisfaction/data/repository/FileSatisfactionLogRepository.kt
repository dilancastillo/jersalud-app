package com.esbot.temi.esbot_health.feature.satisfaction.data.repository

import android.content.Context
import com.esbot.temi.esbot_health.feature.satisfaction.SatisfactionLogStore
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSession
import com.esbot.temi.esbot_health.feature.satisfaction.domain.repository.SatisfactionLogRepository

/**
 * Implementación que guarda sesiones en CSV
 * y dispara la subida a OneDrive usando SatisfactionLogStore.
 * */

class FileSatisfactionLogRepository(
    private val context: Context
) : SatisfactionLogRepository {

    override suspend fun saveSession(session: SatisfactionSession) {
        SatisfactionLogStore.appendSession(context, session)
    }
}