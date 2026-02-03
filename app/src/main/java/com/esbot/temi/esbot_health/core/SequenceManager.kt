package com.esbot.temi.esbot_health.core

import android.util.Log
import com.robotemi.sdk.Robot
import com.esbot.temi.esbot_health.education.EducationTopic
import com.esbot.temi.esbot_health.education.SequencePlayer

object SequenceManager {

    private const val TAG = "SequenceManager"

    /**
     * Reproduce la secuencia según un EducationTopic
     */
    fun play(robot: Robot, topic: EducationTopic) {
        Log.i(TAG, "Reproduciendo secuencia: ${topic.sequenceName}")

        // Mostrar todas las secuencias disponibles en Temi (debug)
        SequencePlayer.debugLogAllSequences(robot)

        // Intentar reproducir la secuencia correspondiente al topic
        val seqId = SequencePlayer.playTopicSequence(robot, topic)
        if (seqId == null) {
            Log.e(TAG, "No se pudo reproducir la secuencia: ${topic.sequenceName}")
        } else {
            Log.i(TAG, "Secuencia iniciada: ${topic.sequenceName} (ID: $seqId)")
        }
    }

    /**
     * Mapear ubicación física → EducationTopic
     * Esto permite que desde MQTT solo envíes la ubicación
     */
    fun getTopicByLocation(location: String): EducationTopic {
        return when(location.lowercase()) {
            "cubiculo1" -> EducationTopic.cubiculo1
            "cubiculo2" -> EducationTopic.cubiculo2
            else -> EducationTopic.RIGHTS_DUTIES // default
        }
    }

    /**
     * Función combinada: recibe ubicación y reproduce automáticamente
     */
    fun playByLocation(robot: Robot, location: String) {
        val topic = getTopicByLocation(location)
        Log.i(TAG, "Ubicación recibida: $location → topic: ${topic.displayName}")
        play(robot, topic)
    }
}
