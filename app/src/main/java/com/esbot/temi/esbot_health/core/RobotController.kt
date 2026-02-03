package com.esbot.temi.esbot_health.core

import android.util.Log
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.esbot.temi.esbot_health.education.EducationTopic

class RobotController(
    private val robot: Robot
) : OnGoToLocationStatusChangedListener {

    private var targetLocation: String? = null
    private var targetTopic: EducationTopic? = null

    init {
        robot.addOnGoToLocationStatusChangedListener(this)
    }

    // Manejar mensaje MQTT
    fun handleMqttLocation(location: String) {
        // Aquí podemos mapear ubicación → topic
        val topic = when(location) {
            "cubiculo1" -> EducationTopic.cubiculo1
            "cubiculo2" -> EducationTopic.cubiculo2
            else -> null
        }

        if (topic != null) {
            goTo(location, topic)
        } else {
            Log.e("RobotController", "No hay topic asociado a $location")
        }
    }

    fun goTo(location: String, topic: EducationTopic) {
        targetLocation = location
        targetTopic = topic

        Log.i("RobotController", "Yendo a $location → luego secuencia ${topic.displayName}")
        robot.goTo(location)
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        if (location != targetLocation) return

        when (status) {
            "complete" -> {
                Log.i("RobotController", "Llegó a $location")

                // Mapear ubicación a topic
                val topic = when (location) {
                    "cubiculo1" -> EducationTopic.cubiculo1
                    "cubiculo2" -> EducationTopic.cubiculo2
                    else -> null
                }

                topic?.let {
                    SequenceManager.play(robot, it)
                }
            }

            "abort", "error" -> {
                Log.e(
                    "RobotController",
                    "Error yendo a $location: $description"
                )
            }
        }
    }



    fun release() {
        robot.removeOnGoToLocationStatusChangedListener(this)
    }
}
