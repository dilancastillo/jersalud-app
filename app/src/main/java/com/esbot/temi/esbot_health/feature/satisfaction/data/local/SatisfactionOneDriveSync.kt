package com.esbot.temi.esbot_health.feature.satisfaction.data.local

import android.content.Context
import com.esbot.temi.esbot_health.feature.satisfaction.OneDriveUploader

class SatisfactionOneDriveSync(
    private val context: Context
) {
    fun uploadLatestCsvAsync() {
        OneDriveUploader.uploadSatisfactionCsvAsync(context)
    }
}