package com.esbot.temi.esbot_health.satisfaction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

class SatisfactionAutoRoundActivity : AppCompatActivity(),
    OnGoToLocationStatusChangedListener,
    Robot.AsrListener {

    private lateinit var robot: Robot

    private lateinit var tvTitle: TextView
    private lateinit var tvStep: TextView
    private lateinit var stepBeds: MaterialCardView
    private lateinit var stepRules: MaterialCardView
    private lateinit var panelRunning: MaterialCardView

    private lateinit var tvBedsFloor: TextView
    private lateinit var layoutBedList: LinearLayout
    private lateinit var btnBedsSelectAll: MaterialButton
    private lateinit var btnBedsClear: MaterialButton

    private lateinit var cbAskAvailability: CheckBox
    private lateinit var cbReturnToNursing: CheckBox

    private lateinit var btnStartRound: MaterialButton
    private lateinit var btnCancelRound: MaterialButton

    private lateinit var tvRunningHeader: TextView
    private lateinit var tvRunningState: TextView
    private lateinit var panelAvailability: LinearLayout
    private lateinit var tvAvailabilityQuestion: TextView
    private lateinit var btnAvailYes: MaterialButton
    private lateinit var btnAvailNo: MaterialButton

    private lateinit var panelQuestion: LinearLayout
    private lateinit var tvQuestion: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var btnVoice: MaterialButton
    private lateinit var btnNextQuestion: MaterialButton

    private val selectedBeds: MutableList<BedInfo> = mutableListOf()
    private var currentBedIndex: Int = -1
    private var currentBed: BedInfo? = null

    private val surveyQuestions = SatisfactionSurvey.shortSurvey()
    private var currentQuestionIndex: Int = 0
    private val currentAnswers: MutableList<SatisfactionAnswer> = mutableListOf()

    private var askAvailability: Boolean = true
    private var returnToNursing: Boolean = false
    private var isRoundRunning: Boolean = false
    private var waitingAvailability: Boolean = false
    private var inQuestionMode: Boolean = false
    private var waitingForNext = false
    private var navigationRetryCount = 0
    private val MAX_NAVIGATION_RETRIES = 5





    private val recordAudioPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startListeningForAnswer()
            } else {
                Toast.makeText(
                    this,
                    "No puedo usar el micrófono sin permiso.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_satisfaction_auto_round)

        robot = Robot.getInstance()

        bindViews()
        setupBeds()
        setupButtons()

        speak("Configura la ronda de satisfacción: primero elige las habitaciones, luego inicia la ronda. Haré pocas preguntas cortas a cada paciente.")
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

    private fun startListeningForAnswer() {
        if (!inQuestionMode) return

        val q = surveyQuestions[currentQuestionIndex]

        val prompt = q.textTts + " Puedes responder ahora en voz alta."

        robot.askQuestion(prompt)
    }

    override fun onAsrResult(asrResult: String, sttLanguage: SttLanguage) {
        runOnUiThread {
            if (!inQuestionMode) return@runOnUiThread

            val transcript = asrResult
                .lowercase(Locale.getDefault())
                .trim()

            if (transcript.isBlank()) return@runOnUiThread

            // CONFIRMACIÓN
            if (waitingForNext) {
                when {
                    transcript.contains("siguiente") ||
                            transcript.contains("continuar") ||
                            transcript.contains("adelante") -> {

                        waitingForNext = false
                        goToNextQuestionByVoice()
                    }

                    transcript.contains("esperar") -> {
                        waitingForNext = false
                        speak("Está bien, puede cambiar su respuesta.")

                        // NO cerrar la conversación
                        android.os.Handler(mainLooper).postDelayed({
                            ensureMicAndListen()
                        }, 1500)
                    }


                    else -> {
                        speak("Por favor responde solo 'continuar' o 'esperar'.")
                    }
                }
                return@runOnUiThread
            }

            // RESPUESTA NORMAL
            handleVoiceAnswer(transcript)
        }
    }


    private fun ensureMicAndListen() {
        if (!inQuestionMode) return
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

    private fun bindViews() {
        tvTitle = findViewById(R.id.tvSatRoundTitle)
        tvStep = findViewById(R.id.tvSatRoundStep)
        stepBeds = findViewById(R.id.stepBedsSat)
        stepRules = findViewById(R.id.stepRulesSat)
        panelRunning = findViewById(R.id.panelSatRunning)

        tvBedsFloor = findViewById(R.id.tvSatBedsFloor)
        layoutBedList = findViewById(R.id.layoutSatBedList)
        btnBedsSelectAll = findViewById(R.id.btnSatBedsSelectAll)
        btnBedsClear = findViewById(R.id.btnSatBedsClear)

        cbAskAvailability = findViewById(R.id.cbSatAskAvailability)
        cbReturnToNursing = findViewById(R.id.cbSatReturnToNursing)

        btnStartRound = findViewById(R.id.btnSatStartRound)
        btnCancelRound = findViewById(R.id.btnSatCancelRound)

        tvRunningHeader = findViewById(R.id.tvSatRunningHeader)
        tvRunningState = findViewById(R.id.tvSatRunningState)
        panelAvailability = findViewById(R.id.panelSatAvailability)
        tvAvailabilityQuestion = findViewById(R.id.tvSatAvailabilityQuestion)
        btnAvailYes = findViewById(R.id.btnSatAvailYes)
        btnAvailNo = findViewById(R.id.btnSatAvailNo)

        panelQuestion = findViewById(R.id.panelSatQuestion)
        tvQuestion = findViewById(R.id.tvSatQuestion)
        rgOptions = findViewById(R.id.rgSatOptions)
        btnVoice = findViewById(R.id.btnSatVoice)
        btnNextQuestion = findViewById(R.id.btnSatNextQuestion)

        cbAskAvailability.isChecked = true
        cbReturnToNursing.isChecked = false

        tvBedsFloor.text = "Piso: ${HospitalConfig.FLOOR_NAME}"
    }

    private fun setupBeds() {
        layoutBedList.removeAllViews()
        for (bed in HospitalConfig.bedsMi) {
            val cb = CheckBox(this).apply {
                text = bed.label
                textSize = 55f
                buttonDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_big
                )
                tag = bed
            }
            layoutBedList.addView(cb)
        }

        btnBedsSelectAll.setOnClickListener {
            for (i in 0 until layoutBedList.childCount) {
                (layoutBedList.getChildAt(i) as? CheckBox)?.isChecked = true
            }
        }

        btnBedsClear.setOnClickListener {
            for (i in 0 until layoutBedList.childCount) {
                (layoutBedList.getChildAt(i) as? CheckBox)?.isChecked = false
            }
        }
    }

    private fun setupButtons() {
        btnStartRound.setOnClickListener {
            askAvailability = cbAskAvailability.isChecked
            returnToNursing = cbReturnToNursing.isChecked
            collectSelectedBeds()

            if (selectedBeds.isEmpty()) {
                Toast.makeText(this, "Seleccione al menos una habitación.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            isRoundRunning = true
            currentBedIndex = -1
            tvStep.text = "Ronda en curso"
            stepBeds.visibility = View.GONE
            stepRules.visibility = View.GONE
            panelRunning.visibility = View.VISIBLE
            goToNextBed()
        }

        btnCancelRound.setOnClickListener {
            if (isRoundRunning) {
                isRoundRunning = false
                speak("Ronda de satisfacción cancelada.")
            }
            finish()
        }

        btnAvailYes.setOnClickListener {
            if (!waitingAvailability || currentBed == null) return@setOnClickListener
            waitingAvailability = false
            panelAvailability.visibility = View.GONE
            startSurveyForCurrentBed()
        }

        btnAvailNo.setOnClickListener {
            if (!waitingAvailability) return@setOnClickListener
            waitingAvailability = false
            speak("De acuerdo, no haremos la encuesta ahora.")
            panelAvailability.visibility = View.GONE
            goToNextBed()
        }

        btnNextQuestion.setOnClickListener {
            if (!inQuestionMode) return@setOnClickListener
            val checkedId = rgOptions.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Selecciona una opción o responde por voz.", Toast.LENGTH_SHORT)
                    .show()
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
                saveSessionAndMoveOn()
            } else {
                showCurrentQuestion()
            }
        }

        btnVoice.setOnClickListener {
            if (!inQuestionMode) return@setOnClickListener
            resetAsrState()
            ensureMicAndListen()
        }
    }

    private fun collectSelectedBeds() {
        selectedBeds.clear()
        for (i in 0 until layoutBedList.childCount) {
            val cb = layoutBedList.getChildAt(i) as? CheckBox ?: continue
            if (cb.isChecked) {
                val bed = cb.tag as? BedInfo ?: continue
                selectedBeds.add(bed)
            }
        }
    }

    private fun goToNextBed() {
        if (!isRoundRunning) return

        currentAnswers.clear()
        navigationRetryCount = 0
        currentQuestionIndex = 0
        inQuestionMode = false

        currentBedIndex++
        if (currentBedIndex >= selectedBeds.size) {
            tvRunningState.text = "Ronda finalizada."
            speak("He terminado la ronda de satisfacción de Instituto Médico Oncológico IMO.")
            if (returnToNursing) {
                speak("Voy a regresar a enfermería.")
                robot.goTo(HospitalConfig.NURSING_LOCATION)
            }
            android.os.Handler(mainLooper).postDelayed({
                robot.finishConversation()
                finish()
            }, 4000)
            isRoundRunning = false
            return
        }

        val bed = selectedBeds[currentBedIndex]
        currentBed = bed
        tvRunningHeader.text = "Ronda en curso – ${bed.label}"
        tvRunningState.text = "Desplazándome a ${bed.label}..."

        speak("Voy a la ${bed.label} para realizar la encuesta de satisfacción.")
        robot.goTo(bed.locationName)
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        runOnUiThread {
            if (!isRoundRunning) return@runOnUiThread

            if (status.equals("complete", ignoreCase = true)) {
                navigationRetryCount = 0
                val bed = currentBed
                if (bed != null) {
                    tvRunningState.text = "En ${bed.label}"
                    if (askAvailability) {
                        askAvailabilityToPatient(bed)
                    } else {
                        startSurveyForCurrentBed()
                    }
                } else {
                    goToNextBed()
                }
            } else if (
                status.equals("abort", true) ||
                status.equals("fail", true) ||
                status.equals("blocked", true) ||
                status.equals("cancel", true) ||
                status.equals("timeout", true)
            ) {

                val bed = currentBed ?: return@runOnUiThread

                when (status.lowercase()) {

                    "blocked", "abort", "timeout" -> {
                        if (navigationRetryCount < MAX_NAVIGATION_RETRIES) {
                            navigationRetryCount++

                            tvRunningState.text =
                                "Camino bloqueado. Reintentando ($navigationRetryCount/$MAX_NAVIGATION_RETRIES)..."

                            robot.speak(
                                TtsRequest.create(
                                    "Hay un obstáculo. Intentaré nuevamente.",
                                    false
                                )
                            )

                            android.os.Handler(mainLooper).postDelayed({
                                robot.goTo(bed.locationName)
                            }, 3000)

                        } else {
                            navigationRetryCount = 0

                            saveFailedNavigation(
                                bed = bed,
                                status = status,
                                reason = description
                            )

                            tvRunningState.text =
                                "No se pudo llegar a la habitación. Saltando a la siguiente."

                            android.os.Handler(mainLooper).postDelayed({
                                goToNextBed()
                            }, 500)
                        }
                    }

                    "fail", "cancel" -> {
                        navigationRetryCount = 0

                        saveFailedNavigation(
                            bed = bed,
                            status = status,
                            reason = description
                        )

                        tvRunningState.text =
                            "Destino no alcanzable. Saltando a la siguiente."

                        android.os.Handler(mainLooper).postDelayed({
                            goToNextBed()
                        }, 500)
                    }
                }
            }



        }
    }

    private fun askAvailabilityToPatient(bed: BedInfo) {
        waitingAvailability = true
        panelAvailability.visibility = View.VISIBLE
        panelQuestion.visibility = View.GONE
        tvAvailabilityQuestion.text = "Hola, soy Temi. ¿Podemos hacerle unas preguntas cortas sobre la atención que ha recibido en Instituto Médico Oncológico IMO?"

        speak("Hola. Soy Temi, el asistente de Instituto Médico Oncológico IMO. ¿Podemos hacerle unas preguntas cortas sobre la atención que ha recibido?")
    }

    private fun startSurveyForCurrentBed() {
        val bed = currentBed ?: return
        waitingAvailability = false
        panelAvailability.visibility = View.GONE
        panelQuestion.visibility = View.VISIBLE
        inQuestionMode = true
        currentAnswers.clear()
        currentQuestionIndex = 0

        speak("Le haré algunas preguntas cortas sobre la atención recibida en Instituto Médico Oncológico IMO. Puede responder por voz.")
        showCurrentQuestion()
    }

    private fun showCurrentQuestion() {
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

//        tvRunningState.text =
//            "Pregunta ${currentQuestionIndex + 1} de ${surveyQuestions.size} en ${currentBed?.label}"
//
//        speak(q.textTts + " Puedes responder diciendo la opción o tocándola en la pantalla.")
        ensureMicAndListen()
    }

    private fun saveSessionAndMoveOn() {
        val bed = currentBed ?: return
        val session = SatisfactionSession(
            timestampMillis = System.currentTimeMillis(),
            mode = "AUTO",
            bed = bed,
            answers = currentAnswers.toList()
        )

        SatisfactionLogStore.appendSession(this, session)

        speak("Gracias por sus respuestas. Esto nos ayuda a mejorar el servicio.")
        panelQuestion.visibility = View.GONE
        inQuestionMode = false

        goToNextBed()
    }
    private fun saveFailedNavigation(
        bed: BedInfo,
        status: String,
        reason: String
    ) {
        val session = SatisfactionSession(
            timestampMillis = System.currentTimeMillis(),
            mode = "AUTO",
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
    }


    private fun handleVoiceAnswer(transcript: String) {
        if (!inQuestionMode) return

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
            }, 3500)

            return
        }

        val rb = rgOptions.getChildAt(index) as? RadioButton
        rb?.isChecked = true

        waitingForNext = true

        android.os.Handler(mainLooper).postDelayed({
            if (waitingForNext) {
                robot.askQuestion(
                    "Registré su respuesta: ${q.options[index]}. " +
                            "Diga 'siguiente' para continuar o diga 'esperar' para cambiar la respuesta."
                )
            }
        }, 2000)

    }
    private fun mapSingleChoice(text: String, options: List<String>): Int? {
        val normalized = text.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()

        // Primero intenta coincidencia exacta o casi exacta
        options.forEachIndexed { index, option ->
            val normalizedOption = option.lowercase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .trim()

            if (normalized == normalizedOption || normalized.contains(normalizedOption)) {
                return index
            }
        }

        // Luego busca palabras clave
        return when {
            normalized.contains("laboral") -> 0
            normalized.contains("fisica") || normalized.contains("terapia") -> 1
            normalized.contains("general") -> 2
            normalized.contains("enfermeria") -> 3
            else -> null
        }
    }

    private fun mapLikert5(text: String): Int? {
        val normalized = text.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()

        return when {
            text.contains("Muy Buena") || text.contains("muy buena") -> 0
            text.contains("Buena") || text.contains("buena") || text.contains("vuela")-> 1
            text.contains("Regular") || text.contains("Regular") -> 2
            text.contains("Muy mala") || text.contains("muy mala") -> 4
            text.contains("Mala") || text.contains("mala") -> 3

            // Regular
            normalized.contains("regular") ||
                    normalized.contains("ni satisfecho") ||
                    normalized.contains("ni insatisfecho") -> 2

            // Fallback numérico
            else -> {
                Regex("""\d""").find(normalized)?.value?.toIntOrNull()?.let {
                    if (it in 1..5) it - 1 else null
                }
            }
        }
    }

    private fun mapRecommend4(text: String): Int? {
        val normalized = text.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()

        return when {
            // Verificar primero las más específicas (con "definitivamente")
            normalized.contains("definitivamente") && (normalized.contains("si") || normalized.contains("sí")) -> 0

            normalized.contains("definitivamente") && normalized.contains("no") -> 3

            // Luego las que tienen "probablemente"
            normalized.contains("probablemente") && (normalized.contains("si") || normalized.contains("sí")) -> 1

            normalized.contains("probablemente") && normalized.contains("no") -> 2

            // Fallback: solo "sí" o "no" (con precaución)
            normalized == "si" || normalized == "sí" -> 1
            normalized == "no" -> 3

            else -> null
        }
    }

    private fun speak(text: String) {
        robot.speak(TtsRequest.create(text, false))
    }
    private fun goToNextQuestionByVoice() {
        resetAsrState()

        val checkedId = rgOptions.checkedRadioButtonId
        if (checkedId == -1) {
            speak("Seleccione una opción antes de continuar.")
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
            saveSessionAndMoveOn()
        } else {
            showCurrentQuestion()
        }
    }

    private fun resetAsrState() {
        waitingForNext = false
        robot.finishConversation()
    }

}