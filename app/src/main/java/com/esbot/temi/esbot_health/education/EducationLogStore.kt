package com.esbot.temi.esbot_health.education

import android.content.Context
import java.io.File
import java.io.FileOutputStream

data class EducationEvent(
    val timestampMillis: Long,
    val topicId: String,
    val topicName: String,
    val bedId: String,
    val bedLabel: String,
    val mode: String,
    val comprehensionLevel: String
//    val needsReplay: Boolean
//    val note: String
)

object EducationLogStore {

    private const val FILE_NAME = "education_log.csv"

    fun appendEvent(context: Context, event: EducationEvent) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val writeHeader = !file.exists() || file.length() == 0L

            FileOutputStream(file, true).use { fos ->
                if (writeHeader) {
                    fos.write(
                        "timestampMillis,topicId,topicName,bedId,bedLabel,mode,comprehensionLevel,needsReplay,note\n"
                            .toByteArray()
                    )
                }

                val safeTopicName = event.topicName.replace("\n", " ").replace(",", " ")
                val safeBedLabel = event.bedLabel.replace("\n", " ").replace(",", " ")
//                val safeNote = event.note.replace("\n", " ").replace(",", " ")

                val line = buildString {
                    append(event.timestampMillis); append(',')
                    append(event.topicId); append(',')
                    append(safeTopicName); append(',')
                    append(event.bedId); append(',')
                    append(safeBedLabel); append(',')
                    append(event.mode); append(',')
                    append(event.comprehensionLevel); append(',')
//                    append(event.needsReplay); append(',')
//                    append(safeNote); append('\n')
                }

                fos.write(line.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}