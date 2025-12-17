package com.esbot.temi.esbot_health.feature.satisfaction.data.local

import android.content.Context
import com.esbot.temi.esbot_health.core.toCsvField
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSession
import java.io.File
import java.io.FileOutputStream

class SatisfactionCsvWriter(
    private val context: Context
) {
    private val fileName = "satisfaction_log.csv"

    fun appendSession(session: SatisfactionSession) {
        val file = File(context.filesDir, fileName)
        val writeHeader = !file.exists() || file.length() == 0L

        FileOutputStream(file, true).use { fos ->
            if (writeHeader) {
                fos.write(
                    "timestampMillis,mode,bedId,bedLabel,questionId,questionLabel,optionIndex,optionText,isRegulatoryKey\n"
                        .toByteArray()
                )
            }

            for (answer in session.answers) {
                val line = buildString {
                    append(session.timestampMillis); append(',')
                    append(session.mode); append(',')
                    append(session.bed.id); append(',')
                    append(session.bed.label.toCsvField()); append(',')
                    append(answer.questionId); append(',')
                    append(answer.questionLabel.toCsvField()); append(',')
                    append(answer.optionIndex); append(',')
                    append(answer.optionText.toCsvField()); append(',')
                    append(answer.isRegulatoryKey); append('\n')
                }
                fos.write(line.toByteArray())
            }
        }
    }

    fun getFile(): File = File(context.filesDir, fileName)
}