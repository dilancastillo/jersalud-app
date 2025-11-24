package com.esbot.temi.esbot_health.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.esbot.temi.esbot_health.R
import com.esbot.temi.esbot_health.satisfaction.SatisfactionAutoRoundActivity
import com.esbot.temi.esbot_health.satisfaction.SatisfactionIndividualActivity
import com.google.android.material.button.MaterialButton
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest

class SatisfactionHomeActivity : AppCompatActivity() {

    private lateinit var robot: Robot
    private lateinit var tvIntro: TextView
    private lateinit var btnAutoRound: MaterialButton
    private lateinit var btnIndividual: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_satisfaction_home)

        robot = Robot.getInstance()

        tvIntro = findViewById(R.id.tvSatIntro)
        btnAutoRound = findViewById(R.id.btnSatAutoRound)
        btnIndividual = findViewById(R.id.btnSatIndividual)

        speak("Estás en el módulo de encuestas de satisfacción. Puedes hacer una ronda corta cama por cama o una sesión individual.")

        btnAutoRound.setOnClickListener {
            val intent = Intent(this, SatisfactionAutoRoundActivity::class.java)
            startActivity(intent)
        }

        btnIndividual.setOnClickListener {
            val intent = Intent(this, SatisfactionIndividualActivity::class.java)
            startActivity(intent)
        }
    }

    private fun speak(text: String) {
        val req = TtsRequest.create(text, false)
        robot.speak(req)
    }
}