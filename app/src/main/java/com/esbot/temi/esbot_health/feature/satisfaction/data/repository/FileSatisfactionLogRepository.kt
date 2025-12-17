package com.esbot.temi.esbot_health.feature.satisfaction.data.repository

import com.esbot.temi.esbot_health.feature.satisfaction.data.local.SatisfactionCsvWriter
import com.esbot.temi.esbot_health.feature.satisfaction.data.local.SatisfactionOneDriveSync
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSession
import com.esbot.temi.esbot_health.feature.satisfaction.domain.repository.SatisfactionLogRepository

class FileSatisfactionLogRepository(
    private val csvWriter: SatisfactionCsvWriter,
    private val oneDriveSync: SatisfactionOneDriveSync
) : SatisfactionLogRepository {

    override suspend fun saveSession(session: SatisfactionSession) {
        // 1) guardar local (CSV)
        csvWriter.appendSession(session)

        // 2) disparar subida a One Drive
        oneDriveSync.uploadLatestCsvAsync()
    }
}