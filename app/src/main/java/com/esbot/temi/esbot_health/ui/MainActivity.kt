package com.esbot.temi.esbot_health.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.esbot.temi.esbot_health.ui.EducationActivity
import com.esbot.temi.esbot_health.ui.PainRoundActivity
import com.robotemi.sdk.Robot
import com.robotemi.sdk.constants.HomeScreenMode
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.listeners.OnRobotReadyListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esbot.temi.esbot_health.R

class MainActivity : AppCompatActivity(), OnRobotReadyListener, OnGoToLocationStatusChangedListener {

    private lateinit var robot: Robot

    private lateinit var tvStatus: TextView
    private lateinit var btnSpeak: Button
    private lateinit var btnGoAdmis: Button
    private lateinit var btnGoHab: Button
    private lateinit var btnBackToBase: Button
    private lateinit var btnPainRound: Button
    private lateinit var btnEducation: Button
    private lateinit var btnExitKiosk: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        robot = Robot.getInstance()

        tvStatus = findViewById(R.id.tvStatus)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnGoAdmis = findViewById(R.id.btnGoAdmis)
        btnGoHab = findViewById(R.id.btnGoHab)
        btnBackToBase = findViewById(R.id.btnBackToBase)
        btnPainRound = findViewById(R.id.btnPainRound)
        btnEducation = findViewById(R.id.btnEducation)
        btnExitKiosk = findViewById(R.id.btnExitKiosk)

        btnSpeak.setOnClickListener {
            say(
                "Hola, soy Temi. Estoy aquí para ayudarte a llegar a tu destino y a acompañarte en tu hospitalización."
            )
        }

        btnGoAdmis.setOnClickListener {
            goToLocation("admisiones")
        }

        btnGoHab.setOnClickListener {
            goToLocation("hab302")
        }

        btnBackToBase.setOnClickListener {
            goToLocation("home base")
        }

        btnPainRound.setOnClickListener {
            val intent = Intent(this, PainRoundActivity::class.java)
            startActivity(intent)
        }

        btnEducation.setOnClickListener {
            val intent = Intent(this, EducationActivity::class.java)
            startActivity(intent)
        }

        btnExitKiosk.setOnClickListener {
            try {
                robot.setKioskModeOn(false, HomeScreenMode.DEFAULT)
                Toast.makeText(this, "Modo kiosko desactivado.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "No se pudo desactivar kiosko.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        robot.addOnRobotReadyListener(this)
        robot.addOnGoToLocationStatusChangedListener(this)
    }

    override fun onStop() {
        robot.removeOnGoToLocationStatusChangedListener(this)
        robot.removeOnRobotReadyListener(this)
        super.onStop()
    }

    override fun onRobotReady(isReady: Boolean) {
        if (!isReady) return

        try {
            robot.setKioskModeOn(true, HomeScreenMode.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        runOnUiThread {
            tvStatus.text = "Estado: Temi listo en Medicina Interna"
        }
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        runOnUiThread {
            tvStatus.text = "Navegación: $location ($status)"
            if (status == "error" || status == "abort") {
                Toast.makeText(
                    this,
                    "Problema yendo a $location: $description",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Helpers Temi
    private fun say(text: String) {
        val ttsRequest = TtsRequest.create(text, false)
        robot.speak(ttsRequest)
    }

    private fun goToLocation(location: String) {
        try {
            robot.goTo(location)
            tvStatus.text = "Estado: yendo a $location"
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "No pude ir a $location", Toast.LENGTH_LONG).show()
        }
    }

    /*private fun goTo(dest: String) {
        robot.goTo(dest)
    }*/

    /*override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        runOnUiThread {
            tvStatus.text = "Navegación: $location ($status)"
        }
    }*/
}