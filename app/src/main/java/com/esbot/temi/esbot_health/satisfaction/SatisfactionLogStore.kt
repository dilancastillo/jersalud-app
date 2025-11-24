package com.esbot.temi.esbot_health.satisfaction

import android.content.Context
import com.esbot.temi.esbot_health.education.BedInfo
import java.io.File
import java.io.FileOutputStream

data class SatisfactionSession(
    val timestampMillis: Long,
    val mode: String,
    val bed: BedInfo,
    val answers: List<SatisfactionAnswer>
)

object SatisfactionLogStore {

    private const val FILE_NAME = "satisfaction_log.csv"

    fun appendSession(context: Context, session: SatisfactionSession) {
        try {
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
                    val safeBedLabel = session.bed.label.replace("\n", " ").replace(",", " ")
                    val safeQLabel = answer.questionLabel.replace("\n", " ").replace(",", " ")
                    val safeOptText = answer.optionText.replace("\n", " ").replace(",", " ")

                    val line = buildString {
                        append(session.timestampMillis); append(',')
                        append(session.mode); append(',')
                        append(session.bed.id); append(',')
                        append(safeBedLabel); append(',')
                        append(answer.questionId); append(',')
                        append(safeQLabel); append(',')
                        append(answer.optionIndex); append(',')
                        append(safeOptText); append(',')
                        append(if (answer.questionId.startsWith("P314") || answer.questionId.startsWith("P315")) "true" else "false")
                        append('\n')
                    }

                    fos.write(line.toByteArray())
                }
            }

            OneDriveUploader.uploadSatisfactionCsvAsync(context)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }
}