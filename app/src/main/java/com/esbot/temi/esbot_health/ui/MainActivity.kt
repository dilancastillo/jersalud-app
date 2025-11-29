package com.esbot.temi.esbot_health.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.esbot.temi.esbot_health.R
import com.robotemi.sdk.Robot
import com.robotemi.sdk.constants.HomeScreenMode
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.listeners.OnRobotReadyListener
import java.util.Locale

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
    private lateinit var btnSatisfaction: MaterialButton
    private lateinit var btnSettings2: android.widget.ImageView

    // ----------------------------------------------------
    // CONSTANTES Y HELPERS DE LOCALIZACIÓN
    // ----------------------------------------------------
    private val PREFS_NAME = "Settings"
    private val KEY_LANG = "MyLang"
    private val DEFAULT_LANG = "es"

    // Helper para leer la preferencia de idioma
    private fun getLocalePreference(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    // Helper para guardar la preferencia de idioma
    private fun saveLocalePreference(context: Context, languageCode: String) {
        val editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString(KEY_LANG, languageCode)
        editor.apply()
    }

    // CRÍTICO: Este método aplica el idioma guardado ANTES de que se cree la Activity
    override fun attachBaseContext(newBase: Context) {
        val languageCode = getLocalePreference(newBase)
        val locale = Locale(languageCode)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    // Función que guarda el idioma y fuerza el reinicio de la Activity
    private fun setLocale(languageCode: String) {
        saveLocalePreference(this, languageCode)
        recreate()
    }
    // ----------------------------------------------------

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
        btnSatisfaction = findViewById(R.id.btnSatisfaction)
        btnExitKiosk = findViewById(R.id.btnExitKiosk)
        btnSettings2 = findViewById(R.id.btnSettings2) as android.widget.ImageView

        // ... [Resto de Listeners de navegación/Temi] ...

        btnSpeak.setOnClickListener {
            // NOTA: Recuerda reemplazar el texto codificado aquí por una referencia a @string/voice_welcome_long_desc
            say(getString(R.string.voice_welcome_long_desc))
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

        btnSatisfaction.setOnClickListener {
            // Nota: Se asume que SatisfactionHomeActivity está definida
            val intent = Intent(this, Class.forName("com.esbot.temi.esbot_health.ui.SatisfactionHomeActivity"))
            startActivity(intent)
        }

        btnExitKiosk.setOnClickListener {
            exitApp()
        }

        // Listener del botón de idioma
        btnSettings2.setOnClickListener {
            val currentLang = getLocalePreference(this)
            val newLang = if (currentLang == "en") "es" else "en"
            setLocale(newLang) // Guardar y recrear Activity
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

    private fun exitApp() {
        try {
            if (robot.isKioskModeOn()) {
                robot.setKioskModeOn(false, HomeScreenMode.CUSTOM_SCREEN)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "No se pudo desactivar modo kiosko, pero la aplicación se cerrará.",
                Toast.LENGTH_LONG
            ).show()
        }

        finishAffinity()
    }

    override fun onRobotReady(isReady: Boolean) {
        if (!isReady) return

        try {
            robot.setKioskModeOn(true, HomeScreenMode.CUSTOM_SCREEN)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        runOnUiThread {
            // NOTA: Reemplaza este texto codificado
            tvStatus.text = "Estado: Temi listo en IPS Jersalud"
        }
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        runOnUiThread {
            // NOTA: Reemplaza este texto codificado
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
            // NOTA: Reemplaza este texto codificado
            tvStatus.text = "Estado: yendo a $location"
        } catch (e: Exception) {
            e.printStackTrace()
            // NOTA: Reemplaza este texto codificado
            Toast.makeText(this, "No pude ir a $location", Toast.LENGTH_LONG).show()
        }
    }
}