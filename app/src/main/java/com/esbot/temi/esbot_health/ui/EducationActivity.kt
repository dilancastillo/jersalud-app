package com.esbot.temi.esbot_health.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esbot.temi.esbot_health.R
import android.content.Intent
import com.esbot.temi.esbot_health.education.EducationLogStore
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.sequence.OnSequencePlayStatusChangedListener
import com.robotemi.sdk.sequence.SequenceModel

class EducationActivity : AppCompatActivity() {

    private lateinit var robot: Robot

    private lateinit var btnModeIndividual: Button
    private lateinit var btnModeAuto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_education)

        robot = Robot.getInstance()

        btnModeIndividual = findViewById(R.id.btnModeIndividual)
        btnModeAuto = findViewById(R.id.btnModeAuto)

        speak("Estás en el módulo de educación al paciente. Puedes elegir una sesión individual o una ronda automática.")

        btnModeIndividual.setOnClickListener {
            val intent = Intent(this, IndividualEducationActivity::class.java)
            startActivity(intent)
        }

        btnModeAuto.setOnClickListener {
            val intent = Intent(this, AutoRoundActivity::class.java)
            startActivity(intent)
        }
    }

    private fun speak(text: String) {
        val req = TtsRequest.create(text, false)
        robot.speak(req)
    }
}