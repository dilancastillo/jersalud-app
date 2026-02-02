package com.esbot.temi.esbot_health.feature.satisfaction.data.local

import com.esbot.temi.esbot_health.feature.satisfaction.data.remote.OneDriveUploader

class SatisfactionOneDriveSync(
    private val csvWriter: SatisfactionCsvWriter
) {
    fun uploadLatestCsvAsync() {
        OneDriveUploader.uploadSatisfactionCsvAsync(csvWriter.getFile())
    }
}