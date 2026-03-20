package com.example.app_advertising.core

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SheetController {
    data class SheetEvent(
        val timestampMillis: Long,
        val topicId: String,
        val topicName: String,
        val bedId: String,
        val bedLabel: String,
        val mode: String,
        val comprehensionLevel: String
    )
    fun sendToSheet(event: SheetEvent) {
        Thread {
            try {
                val url = URL("https://script.google.com/macros/s/AKfycbwID_A-9HNsau154KsDwC6R9a31lKhVN0rI8AUTeeoJmXrwkxygGKelxwCnzzrx2d7B/exec")
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                val json = JSONObject().apply {
                    put("timestampMillis", event.timestampMillis)
                    put("topicId", event.topicId)
                    put("topicName", event.topicName)
                    put("bedId", event.bedId)
                    put("bedLabel", event.bedLabel)
                    put("mode", event.mode)
                    put("comprehensionLevel", event.comprehensionLevel)
                }

                conn.outputStream.use { os ->
                    os.write(json.toString().toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = conn.responseCode
                Log.d("SHEET", "Response code: $responseCode")

                val response = conn.inputStream.bufferedReader().readText()
                Log.d("SHEET", "Response body: $response")

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("SHEET", "Error", e)
            }
        }.start()
    }
}