package com.esbot.temi.esbot_health.education

import android.util.Log
import com.robotemi.sdk.Robot

object SequencePlayer {

    private const val TAG = "SequencePlayer"

    fun debugLogAllSequences(robot: Robot) {
        try {
            val sequences = robot.getAllSequences()
            if (sequences == null) {
                Log.e(TAG, "getAllSequences() devolvió null. ¿Falta permiso de 'Sequence'?")
                return
            }
            Log.d(TAG, "Temi reporta ${sequences.size} secuencias registradas.")
            sequences.forEach { seq ->
                Log.d(TAG, "Seq name='${seq.name}' id='${seq.id}'")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener secuencias", e)
        }
    }

    fun playTopicSequence(robot: Robot, topic: EducationTopic): String? {
        return try {
            val target = topic.sequenceName.trim()
            val sequences = robot.getAllSequences()

            if (sequences == null) {
                Log.e(TAG, "getAllSequences() devolvió null. Revisa permiso de 'Sequence'.")
                return null
            }
            if (sequences.isEmpty()) {
                Log.e(TAG, "getAllSequences() devolvió 0 secuencias. ¿Están creadas/asignadas al Temi?")
                return null
            }

            Log.d(TAG, "Buscando secuencia para topic='${topic.id}' name='$target'")
            Log.d(TAG, "Temi tiene ${sequences.size} secuencias cargadas.")

            sequences.forEach { seq ->
                Log.d(TAG, "Candidata: name='${seq.name}' id='${seq.id}'")
            }

            val exactStrict = sequences.firstOrNull {
                it.name.trim() == target
            }

            val exactIgnoreCase = exactStrict ?: sequences.firstOrNull {
                it.name.trim().equals(target, ignoreCase = true)
            }

            val seq = exactIgnoreCase ?: sequences.firstOrNull {
                it.name.contains(target, ignoreCase = true)
            }

            if (seq == null) {
                Log.e(TAG, "No se encontró secuencia con name parecido a '$target'.")
                return null
            }

            Log.i(TAG, "Usando secuencia name='${seq.name}' id='${seq.id}' para topic='${topic.id}'")

            robot.playSequence(
                seq.id,
                /* withPlayer = */ false,
                /* repeat = */ 1,
                /* startFromStep = */ 1
            )
            seq.id
        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir secuencia para topic='${topic.id}'", e)
            null
        }
    }
}