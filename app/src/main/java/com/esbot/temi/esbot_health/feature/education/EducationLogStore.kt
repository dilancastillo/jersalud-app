package com.esbot.temi.esbot_health.education

import android.content.Context
import com.esbot.temi.esbot_health.core.toCsvField
import java.io.File
import java.io.FileOutputStream

data class EducationEvent(
    val timestampMillis: Long,
    val topicId: String,
    val topicName: String,
    val bedId: String,
    val bedLabel: String,
    val mode: EducationMode,
    val comprehensionLevel: ComprehensionLevel,
    val needsReplay: Boolean,
    val note: String
)

object EducationLogStore {

    private const val FILE_NAME = "education_log.csv"

    fun appendEvent(context: Context, event: EducationEvent) {
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            val writeHeader = !file.exists() || file.length() == 0L

            FileOutputStream(file, true).use { fos ->
                if (writeHeader) {
                    fos.write(
                        "timestampMillis,topicId,topicName,bedId,bedLabel,mode,comprehensionLevel,needsReplay,note\n"
                            .toByteArray()
                    )
                }

                val line = buildString {
                    append(event.timestampMillis); append(',')
                    append(event.topicId); append(',')
                    append(event.topicName.toCsvField()); append(',')
                    append(event.bedId); append(',')
                    append(event.bedLabel.toCsvField()); append(',')
                    append(event.mode.name); append(',')
                    append(event.comprehensionLevel.name); append(',')
                    append(event.needsReplay); append(',')
                    append(event.note.toCsvField()); append('\n')
                }

                fos.write(line.toByteArray())
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }

    fun getLocalFile(context: Context): File =
        File(context.filesDir, FILE_NAME)
}