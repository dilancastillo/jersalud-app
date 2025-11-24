package com.esbot.temi.esbot_health.satisfaction

import android.content.Context
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object OneDriveUploader {

    private const val TAG = "OneDriveUploader"

    private const val GRAPH_UPLOAD_URL =
        "https://graph.microsoft.com/v1.0/me/drive/root:/Temi/satisfaction_log.csv:/content"

    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String) {
        accessToken = token
    }

    fun uploadSatisfactionCsvAsync(context: Context) {
        val token = accessToken
        if (token.isNullOrBlank()) {
            Log.w(TAG, "No hay accessToken configurado. No se subirá a OneDrive.")
            return
        }

        val file = SatisfactionLogStore.getLocalFile(context)
        if (!file.exists() || file.length() == 0L) {
            Log.w(TAG, "Archivo de log vacío o inexistente, no se sube.")
            return
        }

        thread {
            uploadFileToOneDrive(file, token)
        }
    }

    private fun uploadFileToOneDrive(file: File, token: String) {
        try {
            val url = URL(GRAPH_UPLOAD_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "text/csv")
            }

            FileInputStream(file).use { fis ->
                BufferedOutputStream(conn.outputStream).use { os ->
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (fis.read(buffer).also { len = it } != -1) {
                        os.write(buffer, 0, len)
                    }
                    os.flush()
                }
            }

            val code = conn.responseCode
            if (code in 200..299) {
                Log.i(TAG, "Archivo de satisfacción subido correctamente a OneDrive. Código HTTP: $code")
            } else {
                Log.e(TAG, "Error al subir a OneDrive. Código HTTP: $code")
            }

            conn.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al subir archivo a OneDrive: ${e.message}", e)
        }
    }
}