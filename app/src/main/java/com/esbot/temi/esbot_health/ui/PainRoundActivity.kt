package com.esbot.temi.esbot_health.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.esbot.temi.esbot_health.R
import java.util.Locale

class PainRoundActivity : AppCompatActivity(), OnGoToLocationStatusChangedListener {

    private val robot: Robot by lazy { Robot.getInstance() }

    private lateinit var progress: ProgressBar
    private lateinit var tvResult: TextView
    private lateinit var etRoom: EditText
    private lateinit var swNeedHelp: Switch
    private lateinit var btnListen: Button
    private lateinit var btnSend: Button
    private lateinit var keypad: LinearLayout

    private var speechRecognizer: SpeechRecognizer? = null
    private var currentNrs: Int? = null

    private var pendingRoom: String? = null
    private var pendingNrs: Int? = null
    private var pendingNeedHelp: Boolean = false
    private var goingToNursing: Boolean = false

    private val recordAudioPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
        else Toast.makeText(this, "Permiso de micrófono denegado.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pain_round)

        progress = findViewById(R.id.progress)
        tvResult = findViewById(R.id.tvResult)
        etRoom = findViewById(R.id.etRoom)
        swNeedHelp = findViewById(R.id.swNeedHelp)
        btnListen = findViewById(R.id.btnListen)
        btnSend = findViewById(R.id.btnSend)
        keypad = findViewById(R.id.keypad)

        speak("Le haré una pregunta sobre su dolor. Diga un número de cero a diez, o toque un número en la pantalla.")

        btnListen.setOnClickListener { ensureMicAndListen() }

        btnSend.setOnClickListener {
            val room = etRoom.text.toString().ifBlank { "Hab_302" }
            val nrs = currentNrs
            if (nrs == null) {
                Toast.makeText(this, "Indique el dolor de cero a diez, por voz o con el teclado.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            triggerPhysicalAlert(room, nrs, swNeedHelp.isChecked)
        }

        setupKeypad()
    }

    override fun onStart() {
        super.onStart()
        robot.addOnGoToLocationStatusChangedListener(this)
    }

    override fun onStop() {
        robot.removeOnGoToLocationStatusChangedListener(this)
        super.onStop()
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    private fun setupKeypad() {
        val numbers = (0..10).toList()
        keypad.removeAllViews()
        for (n in numbers) {
            val b = Button(this).apply {
                text = n.toString()
                textSize = 20f
                setOnClickListener {
                    currentNrs = n
                    tvResult.text = "Dolor (NRS): $n"
                    if (n >= 7) {
                        speak("Dolor alto registrado: $n de diez.")
                    } else {
                        speak("Dolor registrado: $n de diez.")
                    }
                }
            }

            val lp = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            lp.setMargins(dp(4), dp(4), dp(4), dp(4))
            b.layoutParams = lp
            keypad.addView(b)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun ensureMicAndListen() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            recordAudioPerm.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Reconocimiento de voz no disponible en este dispositivo.", Toast.LENGTH_LONG).show()
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    progress.visibility = View.VISIBLE
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    progress.visibility = View.GONE
                    Toast.makeText(
                        this@PainRoundActivity,
                        "No entendí. Intenta de nuevo o usa los botones.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResults(results: Bundle) {
                    progress.visibility = View.GONE
                    val list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
                    val transcript = list.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
                    val parsed = parseSpanishNumber(transcript)
                    if (parsed != null && parsed in 0..10) {
                        currentNrs = parsed
                        tvResult.text = "Dolor (NRS): $parsed"
                        if (parsed >= 7) {
                            speak("Dolor alto registrado: $parsed de diez.")
                        } else {
                            speak("Dolor registrado: $parsed de diez.")
                        }
                    } else {
                        Toast.makeText(
                            this@PainRoundActivity,
                            "Responde un número entre cero y diez.",
                            Toast.LENGTH_SHORT
                        ).show()
                        speak("No entendí el número. Puedes intentar de nuevo, o tocar el número en pantalla.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga un número de cero a diez")
        }
        speechRecognizer?.startListening(intent)
    }

    // Conversión simple de texto a número
    private fun parseSpanishNumber(text: String): Int? {
        val cleaned = text.trim().lowercase(Locale.getDefault())

        Regex("""\d{1,2}""").find(cleaned)?.value?.toIntOrNull()?.let {
            if (it in 0..10) return it
        }

        val map = mapOf(
            "cero" to 0,
            "uno" to 1, "una" to 1,
            "dos" to 2,
            "tres" to 3,
            "cuatro" to 4,
            "cinco" to 5,
            "seis" to 6,
            "siete" to 7,
            "ocho" to 8,
            "nueve" to 9,
            "diez" to 10
        )
        for ((k, v) in map) {
            if (cleaned.contains(k)) return v
        }
        return null
    }

    private fun speak(text: String) {
        robot.speak(TtsRequest.create(text, false))
    }

    private fun triggerPhysicalAlert(room: String, nrs: Int, needHelp: Boolean) {
        pendingRoom = room
        pendingNrs = nrs
        pendingNeedHelp = needHelp || (nrs >= 7)
        goingToNursing = true

        if (pendingNeedHelp) {
            speak("Voy a Enfermería para avisar sobre un dolor de $nrs de diez en la habitación $room.")
        } else {
            speak("Registraré la información en Enfermería. Me dirigiré allí.")
        }

        // Nombre EXACTO de la locación en el mapa Temi
        robot.goTo("Enfermeria")
    }

    override fun onGoToLocationStatusChanged(location: String, status: String, descriptionId: Int, description: String) {
        if (!goingToNursing) return
        if (location != "Enfermeria") return

        if (status == "complete") {
            goingToNursing = false

            val room = pendingRoom ?: "Habitación sin especificar"
            val nrs = pendingNrs ?: -1
            val needHelpLocal = pendingNeedHelp

            val msg = if (nrs >= 0) {
                if (needHelpLocal) {
                    "Atención Enfermería: el paciente en la $room reporta dolor de $nrs sobre diez y necesita ayuda."
                } else {
                    "Atención Enfermería: el paciente en la $room reporta dolor de $nrs sobre diez."
                }
            } else {
                "Atención Enfermería: hay un registro de dolor pendiente para revisión."
            }

            runOnUiThread {
                speak(msg)
                Toast.makeText(
                    this,
                    "Mensaje de dolor anunciado en Enfermería.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
