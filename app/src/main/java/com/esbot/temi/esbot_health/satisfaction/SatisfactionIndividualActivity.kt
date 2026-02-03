package com.esbot.temi.esbot_health.satisfaction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.esbot.temi.esbot_health.R
import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.education.HospitalConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.SttLanguage
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import java.util.Locale

class SatisfactionIndividualActivity : AppCompatActivity()
    ,

    OnGoToLocationStatusChangedListener,
    Robot.AsrListener {

    private lateinit var robot: Robot

    private lateinit var tvTitle: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvFloor: TextView

    private lateinit var stepBed: MaterialCardView
    private lateinit var panelSurvey: MaterialCardView

    private lateinit var rgBeds: RadioGroup
    private lateinit var rgOptions: RadioGroup
    private lateinit var btnGo: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnVoice: MaterialButton
    private lateinit var btnNextQuestion: MaterialButton

    private val surveyQuestions = SatisfactionSurvey.shortSurvey()
    private var currentQuestionIndex: Int = 0
    private val currentAnswers: MutableList<SatisfactionAnswer> = mutableListOf()

    private var selectedBed: BedInfo? = null
    private var inSurvey: Boolean = false
    private var waitingForNext = false
    private var navigationHandled = false
    private var inQuestionMode: Boolean = false
    private var navigationRetryCount = 0
    private val MAX_NAVIGATION_RETRIES = 5




    private val recordAudioPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListeningForAnswer()
        else Toast.makeText(this, "Permiso de micrófono denegado.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_satisfaction_individual)

        robot = Robot.getInstance()
        bindViews()
        setupBeds()
        setupButtons()

        speak("Selecciona primero la habitación del paciente. Después iré hasta allí y realizaremos una encuesta corta de satisfacción.")
    }

    override fun onStart() {
        super.onStart()
        robot.addOnGoToLocationStatusChangedListener(this)
        robot.addAsrListener(this)
    }

    override fun onStop() {
        robot.removeOnGoToLocationStatusChangedListener(this)
        robot.removeAsrListener(this)
        super.onStop()
    }

    override fun onAsrResult(asrResult: String, sttLanguage: SttLanguage) {
        if (!inSurvey) return
        runOnUiThread {
            val transcript = asrResult.lowercase(Locale.getDefault()).trim()
            if (transcript.isBlank()) return@runOnUiThread

            // Manejo de confirmación para pasar a la siguiente pregunta
            if (waitingForNext) {
                when {
                    // Solo respuestas cortas y directas como confirmación
                    transcript.contains("siguiente") ||
                    transcript.contains("continuar") ||
                    transcript.contains("adelante") ||
                    transcript.contains("avanzar") ||
                    transcript.contains("continua") -> {
                        waitingForNext = false
                        if (currentQuestionIndex >= surveyQuestions.size - 1) {
                            val checkedId = rgOptions.checkedRadioButtonId
                            if (checkedId != -1) {
                                val index = rgOptions.indexOfChild(findViewById(checkedId))
                                val q = surveyQuestions[currentQuestionIndex]
                                val optText = (findViewById<RadioButton>(checkedId)).text.toString()

                                currentAnswers.add(
                                    SatisfactionAnswer(
                                        questionId = q.id,
                                        questionLabel = q.label,
                                        optionIndex = index,
                                        optionText = optText
                                    )
                                )
                            }
                            saveSessionAndFinish()
                        } else {
                            goToNextQuestion()
                        }
                    }
                    transcript.contains("esperar") ||
                            transcript.contains("quedarse") -> {

                        waitingForNext = false

                        // NO cerrar conversación
                        Handler(mainLooper).postDelayed({
                            ensureMicAndListen() // vuelve a escuchar la MISMA pregunta
                        }, 1200)
                    }
                    else -> {
                        speak("Por favor diga 'siguiente' para continuar o 'esperar' para cambiar la respuesta.")

                        Handler(mainLooper).postDelayed({
                            if (waitingForNext) {
                                robot.askQuestion(
                                    "Por favor diga 'siguiente' para continuar o 'esperar' para cambiar la respuesta."
                                )
                            }
                        }, 2000)
                    }
                }
                return@runOnUiThread
            }

            if (inSurvey) handleVoiceAnswer(transcript)
        }

    }


    private fun bindViews() {
        tvTitle = findViewById(R.id.tvSatIndTitle)
        tvQuestion = findViewById(R.id.tvSatIndQuestion)
        tvFloor = findViewById(R.id.tvSatIndFloor)

        stepBed = findViewById(R.id.stepSatIndBed)
        panelSurvey = findViewById(R.id.panelSatIndSurvey)

        rgBeds = findViewById(R.id.rgSatIndBeds)
        rgOptions = findViewById(R.id.rgSatIndOptions)

        btnGo = findViewById(R.id.btnSatIndGo)
        btnCancel = findViewById(R.id.btnSatIndCancel)
        btnVoice = findViewById(R.id.btnSatIndVoice)
        btnNextQuestion = findViewById(R.id.btnSatIndNextQuestion)

        tvFloor.text = "Piso: ${HospitalConfig.FLOOR_NAME}"
    }

    private fun setupBeds() {
        rgBeds.removeAllViews()
        for ((index, bed) in HospitalConfig.bedsMi.withIndex()) {
            val rb = RadioButton(this).apply {
                text = bed.label
                id = View.generateViewId()
                textSize = 60f
                buttonDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_big
                )
                tag = bed
            }
            rgBeds.addView(rb)
            if (index == 0) rb.isChecked = true
        }
    }

    private fun setupButtons() {
        btnGo.setOnClickListener {
            val checkedId = rgBeds.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Seleccione una habitación.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val rb = findViewById<RadioButton>(checkedId)
            val bed = rb.tag as? BedInfo ?: return@setOnClickListener

            selectedBed = bed
            speak("Voy a la ${bed.label} para realizar la encuesta de satisfacción.")
            robot.goTo(bed.locationName)
        }

        btnCancel.setOnClickListener {
            if (inSurvey) {
                speak("Sesión de satisfacción cancelada.")
            }
            finish()
        }

        btnVoice.setOnClickListener {
            resetAsrState()
            ensureMicAndListen()
        }

        btnNextQuestion.setOnClickListener {
            if (!inSurvey) return@setOnClickListener
            val checkedId = rgOptions.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(
                    this,
                    "Selecciona una opción o responde por voz.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            resetAsrState()

            val index = rgOptions.indexOfChild(findViewById(checkedId))
            val q = surveyQuestions[currentQuestionIndex]
            val optText = (findViewById<RadioButton>(checkedId)).text.toString()

            currentAnswers.add(
                SatisfactionAnswer(
                    questionId = q.id,
                    questionLabel = q.label,
                    optionIndex = index,
                    optionText = optText
                )
            )

            currentQuestionIndex++
            if (currentQuestionIndex >= surveyQuestions.size) {
                saveSessionAndFinish()
            } else {
                showCurrentQuestion()
            }
        }
    }
    private fun goToNextQuestion() {
        if (!inSurvey) return

        val checkedId = rgOptions.checkedRadioButtonId
        if (checkedId == -1) {
            Toast.makeText(this, "Selecciona una opción o responde por voz.", Toast.LENGTH_SHORT).show()
            return
        }

        val index = rgOptions.indexOfChild(findViewById(checkedId))
        val q = surveyQuestions[currentQuestionIndex]
        val optText = (findViewById<RadioButton>(checkedId)).text.toString()

        currentAnswers.add(
            SatisfactionAnswer(
                questionId = q.id,
                questionLabel = q.label,
                optionIndex = index,
                optionText = optText
            )
        )

        
        currentQuestionIndex++
        if (currentQuestionIndex >= surveyQuestions.size) {
            saveSessionAndFinish()
        } else {
            showCurrentQuestion()
        }
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        runOnUiThread {
            val bed = selectedBed ?: return@runOnUiThread
            if (!location.equals(bed.locationName, ignoreCase = true)) return@runOnUiThread
            if (navigationHandled) return@runOnUiThread

            when (status.lowercase()) {
                "complete" -> {
                    navigationHandled = true
                    startSurvey()
                }

                "blocked", "abort", "timeout" -> {
                    if (navigationRetryCount < MAX_NAVIGATION_RETRIES) {
                        navigationRetryCount++

                        robot.speak(
                            TtsRequest.create(
                                "Hay un obstáculo en el camino. Intentaré nuevamente.",
                                false
                            )
                        )

                        Handler(mainLooper).postDelayed({
                            robot.goTo(bed.locationName)
                        }, 3000) // espera 3 segundos
                    } else {
                        navigationHandled = true
                        saveFailedNavigation(status, description)
                    }
                }

                "fail", "cancel" -> {
                    navigationHandled = true
                    saveFailedNavigation(status, description)
                }

            }
        }
    }


    private fun startSurvey() {
        val bed = selectedBed ?: return
        inSurvey = true
        stepBed.visibility = View.GONE
        panelSurvey.visibility = View.VISIBLE

        currentAnswers.clear()
        currentQuestionIndex = 0

        speak("Estamos en la ${bed.label}. Le haré algunas preguntas cortas sobre la atención recibida en Instituto Médico Oncológico IMO, responde por voz.")
        showCurrentQuestion()
    }

    private fun showCurrentQuestion() {
        resetAsrState()
        val q = surveyQuestions[currentQuestionIndex]
        tvQuestion.text = q.label
        rgOptions.removeAllViews()

        for (opt in q.options) {
            val rb = RadioButton(this).apply {
                text = opt
                textSize = 55f
                buttonDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_big
                )

            }
            rgOptions.addView(rb)
        }
        ensureMicAndListen()
        //speak(q.textTts + " Puedes responder diciendo la opción o tocándola en la pantalla.")
    }

    private fun saveSessionAndFinish() {
        val bed = selectedBed ?: return
        val session = SatisfactionSession(
            timestampMillis = System.currentTimeMillis(),
            mode = "INDIVIDUAL",
            bed = bed,
            answers = currentAnswers.toList()
        )
        SatisfactionLogStore.appendSession(this, session)

        robot.finishConversation()
        waitingForNext = false
        inSurvey = false

        // Hablar
        robot.speak(
            TtsRequest.create(
                "Gracias por sus respuestas. Esto nos ayuda a mejorar la calidad de la atención.",
                false
            )
        )

        Log.i("saved", "Respuesta guardada")

        Handler(mainLooper).postDelayed({
            robot.goTo("home base")
            finish()
        }, 3500)
    }
    private fun saveFailedNavigation(status: String, reason: String) {
        val bed = selectedBed ?: return

        val session = SatisfactionSession(
            timestampMillis = System.currentTimeMillis(),
            mode = "INDIVIDUAL",
            bed = bed,
            answers = listOf(
                SatisfactionAnswer(
                    questionId = "NAVIGATION",
                    questionLabel = "Navigation failed",
                    optionIndex = -1,
                    optionText = "status=$status | reason=$reason"
                )
            )
        )

        SatisfactionLogStore.appendSession(this, session)

        robot.finishConversation()
        robot.speak(
            TtsRequest.create(
                "No pude llegar a la habitación. El evento fue registrado.",
                false
            )
        )


        Handler(mainLooper).postDelayed({
            finish()
            robot.goTo("home base")
            }, 3500)
    }


    private fun ensureMicAndListen() {
        if (!inSurvey) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            recordAudioPerm.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startListeningForAnswer()
        }
    }

    private fun startListeningForAnswer() {
        if (!inSurvey) return

        val q = surveyQuestions[currentQuestionIndex]
        val prompt = q.textTts + " Puedes responder ahora en voz alta."

        robot.askQuestion(prompt)
    }

    private fun handleVoiceAnswer(transcript: String) {
        if (!inSurvey) return
        val q = surveyQuestions[currentQuestionIndex]
        val cleaned = transcript.trim().lowercase(Locale.getDefault())

        val index = when (q.type) {
            SatisfactionQuestionType.LIKERT_5 -> mapLikert5(cleaned)
            SatisfactionQuestionType.RECOMMEND_4 -> mapRecommend4(cleaned)
            SatisfactionQuestionType.SINGLE_CHOICE_LIST -> mapSingleChoice(cleaned, q.options)
        }

        if (index == null || index !in q.options.indices) {
            waitingForNext = false
            robot.finishConversation()
            speak(
                "No entendí la respuesta. " +
                        "Puede repetirla de nuevo."
            )

            //  volver a escuchar la MISMA pregunta
            android.os.Handler(mainLooper).postDelayed({
                ensureMicAndListen()
            }, 1500)

            return
        }

        val rb = rgOptions.getChildAt(index) as? RadioButton
        rb?.isChecked = true

        waitingForNext = true

        // Usar un Handler para hacer la pregunta DESPUÉS de un delay
        android.os.Handler(mainLooper).postDelayed({
            if (waitingForNext) {  // Verificar que todavía estamos esperando
                robot.askQuestion("Registré su respuesta: ${q.options[index]}. ¿Desea pasar a la siguiente pregunta? Diga 'siguiente' para continuar o diga 'esperar' para cambiar la respuesta.")
            }
        }, 1000)  // Esperar 2 segundo
    }
//    private fun mapSingleChoice(text: String, options: List<String>): Int? {
//        val normalized = text.lowercase()
//            .replace("á", "a")
//            .replace("é", "e")
//            .replace("í", "i")
//            .replace("ó", "o")
//            .replace("ú", "u")
//            .trim()
//
//        // Primero intenta coincidencia exacta o casi exacta
//        options.forEachIndexed { index, option ->
//            val normalizedOption = option.lowercase()
//                .replace("á", "a")
//                .replace("é", "e")
//                .replace("í", "i")
//                .replace("ó", "o")
//                .replace("ú", "u")
//                .trim()
//
//            if (normalized == normalizedOption || normalized.contains(normalizedOption)) {
//                return index
//            }
//        }
//
//        // Luego busca palabras clave
//        return when {
//            normalized.contains("laboral") -> 0
//            normalized.contains("fisica") || normalized.contains("terapia") -> 1
//            normalized.contains("general") -> 2
//            normalized.contains("enfermeria") -> 3
//            else -> null
//        }
//    }


//    private fun mapLikert5(text: String): Int? {
//        val normalized = text.lowercase()
//            .replace("á", "a")
//            .replace("é", "e")
//            .replace("í", "i")
//            .replace("ó", "o")
//            .replace("ú", "u")
//            .trim()
//
//        return when {
//            text.contains("Muy Buena") || text.contains("muy buena") -> 0
//            text.contains("Buena") || text.contains("buena") || text.contains("vuela")-> 1
//            text.contains("Regular") || text.contains("Regular") -> 2
//            text.contains("Muy mala") || text.contains("muy mala") -> 4
//            text.contains("Mala") || text.contains("mala") -> 3
//
//            // Regular
//            normalized.contains("regular") ||
//                    normalized.contains("ni satisfecho") ||
//                    normalized.contains("ni insatisfecho") -> 2
//
//            // Fallback numérico
//            else -> {
//                Regex("""\d""").find(normalized)?.value?.toIntOrNull()?.let {
//                    if (it in 1..5) it - 1 else null
//                }
//            }
//        }
//    }
private fun mapLikert5(text: String): Int? {
    val normalized = text.lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .trim()

    return when {
        normalized.contains("muy buena") -> 0
        normalized.contains("buena") -> 1
        normalized.contains("regular") || normalized.contains("ni satisfecho") || normalized.contains("ni insatisfecho") -> 2
        normalized.contains("mala") -> 3
        normalized.contains("muy mala") -> 4
        // Fallback numérico 1-5
        else -> Regex("""\d""").find(normalized)?.value?.toIntOrNull()?.let {
            if (it in 1..5) it - 1 else null
        }
    }
}


//    private fun mapRecommend4(text: String): Int? {
//        val normalized = text.lowercase()
//            .replace("á", "a")
//            .replace("é", "e")
//            .replace("í", "i")
//            .replace("ó", "o")
//            .replace("ú", "u")
//            .trim()
//
//        return when {
//            // Verificar primero las más específicas (con "definitivamente")
//            normalized.contains("definitivamente") && (normalized.contains("si") || normalized.contains("sí")) -> 0
//
//            normalized.contains("definitivamente") && normalized.contains("no") -> 3
//
//            // Luego las que tienen "probablemente"
//            normalized.contains("probablemente") && (normalized.contains("si") || normalized.contains("sí")) -> 1
//
//            normalized.contains("probablemente") && normalized.contains("no") -> 2
//
//            // Fallback: solo "sí" o "no" (con precaución)
//            normalized == "si" || normalized == "sí" -> 1
//            normalized == "no" -> 3
//
//            else -> null
//        }
//    }
private fun mapRecommend4(text: String): Int? {
    val normalized = text.lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .trim()

    return when {
        normalized.contains("definitivamente") && (normalized.contains("si") || normalized.contains("sí")) -> 0
        normalized.contains("probablemente") && (normalized.contains("si") || normalized.contains("sí")) -> 1
        normalized.contains("probablemente") && normalized.contains("no") -> 2
        normalized.contains("definitivamente") && normalized.contains("no") -> 3
        // fallback simple por "si"/"no"
        normalized == "si" || normalized == "sí" -> 1
        normalized == "no" -> 3
        else -> null
    }
}
    private fun mapSingleChoice(text: String, options: List<String>): Int? {
        val normalized = text.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()

        // Primero coincidencia exacta o parcial con la lista de opciones
        options.forEachIndexed { index, option ->
            val optNormalized = option.lowercase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .trim()

            if (normalized == optNormalized || normalized.contains(optNormalized)) {
                return index
            }
        }

        // Palabras clave generales (opcional, según tu contexto)
        return when {
            normalized.contains("si") -> options.indexOfFirst { it.lowercase().contains("si") }.takeIf { it != -1 }
            normalized.contains("no") -> options.indexOfFirst { it.lowercase().contains("no") }.takeIf { it != -1 }
            normalized.contains("n/a") -> options.indexOfFirst { it.lowercase().contains("n/a") }.takeIf { it != -1 }
            else -> null
        }
    }



    private fun speak(text: String) {
        robot.speak(TtsRequest.create(text, false))
    }
    private fun resetAsrState() {
        waitingForNext = false
        robot.finishConversation() // CIERRA el ciclo ASR actual
    }

}