package com.esbot.temi.esbot_health.core

import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory

class MqttController(
    private val robotController: RobotController, // Inyectamos el controller
    private val onStatus: (String) -> Unit
){

    private var client: MqttClient? = null

    fun connect() {
        try {
            client = MqttClient(
                "ssl://c91d1798a8a74ab68c913b5a83204947.s1.eu.hivemq.cloud:8883",
                "TemiRobot_${System.currentTimeMillis()}",
                MemoryPersistence()
            )

            val options = MqttConnectOptions().apply {
                userName = "andrespa02"
                password = "Comfer1234.".toCharArray()
                isAutomaticReconnect = true
                socketFactory = SSLSocketFactory.getDefault()
            }

            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    onStatus("MQTT desconectado")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (message == null) return

                    val payload = String(message.payload).trim()
                    Log.d("MQTT", "Mensaje recibido: $payload")

                    // Enviar al robot
                    robotController.handleMqttLocation(payload)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options)
            client?.subscribe("puerta/sensor", 1)

            onStatus("MQTT conectado")

        } catch (e: Exception) {
            Log.e("MQTT", "Error MQTT", e)
            onStatus("Error MQTT")
        }
    }

    fun disconnect() {
        try {
            client?.disconnect()
            client?.close()
        } catch (_: Exception) {}
    }
}
