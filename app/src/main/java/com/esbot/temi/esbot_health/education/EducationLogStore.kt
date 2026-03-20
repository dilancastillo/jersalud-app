package com.esbot.temi.esbot_health.education

import android.content.Context
import com.example.app_advertising.core.SheetController
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
                        "timestampMillis,topicId,topicName,bedId,bedLabel,mode,comprehensionLevel\n"
                            .toByteArray()
                    )
                }

                val safeTopicName = event.topicName.replace("\n", " ").replace(",", " ")
                val safeBedLabel = event.bedLabel.replace("\n", " ").replace(",", " ")
//                val safeNote = event.note.replace("\n", " ").replace(",", " ")

                val line = buildString {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(event.timestampMillis))

                    append(date); append(',')
                    append(event.topicId); append(',')
                    append(safeTopicName); append(',')
                    append(event.bedId); append(',')
                    append(safeBedLabel); append(',')
                    append(event.mode); append(',')
                    append(event.comprehensionLevel);
//                    append(event.needsReplay); append(',')
                    append('\n')
                }

                fos.write(line.toByteArray())
                try {
                    sendEducationEvent(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun sendEducationEvent(event: com.esbot.temi.esbot_health.education.EducationEvent) {

        val sheetEvent = SheetController.SheetEvent(
            timestampMillis = event.timestampMillis,
            topicId = event.topicId,
            topicName = event.topicName,
            bedId = event.bedId,
            bedLabel = event.bedLabel,
            mode = event.mode,
            comprehensionLevel = event.comprehensionLevel
        )

        SheetController().sendToSheet(sheetEvent)
    }
}