package com.esbot.temi.esbot_health.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.esbot.temi.esbot_health.R
import com.esbot.temi.esbot_health.satisfaction.SatisfactionAutoRoundActivity
import com.esbot.temi.esbot_health.satisfaction.SatisfactionIndividualActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest

class SatisfactionHomeActivity : AppCompatActivity() {

    private lateinit var robot: Robot
    private lateinit var tvIntro: TextView
    private lateinit var btnAutoRound: MaterialCardView
    private lateinit var btnIndividual: MaterialCardView
    private lateinit var btnHome: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_satisfaction_home)

        robot = Robot.getInstance()

        btnHome = findViewById(R.id.btnSatHome)
        tvIntro = findViewById(R.id.tvSatIntro)
        btnAutoRound = findViewById(R.id.btnSatAutoRound)
        btnIndividual = findViewById(R.id.btnSatIndividual)

        speak("Estás en el módulo de encuestas de satisfacción del Instituto Médico Oncológico IMO. Puedes hacer una ronda corta habitación por habitación o una sesión individual.")

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish()
        }

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