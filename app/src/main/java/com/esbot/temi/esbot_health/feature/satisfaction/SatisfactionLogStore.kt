package com.esbot.temi.esbot_health.feature.satisfaction

import android.content.Context
import com.esbot.temi.esbot_health.core.toCsvField
import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionAnswer
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSession
import java.io.File
import java.io.FileOutputStream

object SatisfactionLogStore {

    private const val FILE_NAME = "satisfaction_log.csv"

    fun appendSession(context: Context, session: SatisfactionSession) {
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
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

            OneDriveUploader.uploadSatisfactionCsvAsync(context)

        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }

    fun getLocalFile(context: Context): File =
        File(context.filesDir, FILE_NAME)
}