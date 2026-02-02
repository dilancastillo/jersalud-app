package com.esbot.temi.esbot_health.feature.satisfaction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.esbot.temi.esbot_health.R
import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.education.HospitalConfig
import com.esbot.temi.esbot_health.feature.satisfaction.data.local.SatisfactionCsvWriter
import com.esbot.temi.esbot_health.feature.satisfaction.data.local.SatisfactionOneDriveSync
import com.esbot.temi.esbot_health.feature.satisfaction.data.repository.FileSatisfactionLogRepository
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionAnswer
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionQuestionType
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSurvey
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionMode
import com.esbot.temi.esbot_health.feature.satisfaction.domain.usecase.FinishSatisfactionRoundUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.SttLanguage
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import java.util.Locale

class SatisfactionIndividualActivity : AppCompatActivity(),
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

    private val recordAudioPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListeningForAnswer()
        else Toast.makeText(this, "Permiso de micrófono denegado.", Toast.LENGTH_SHORT).show()
    }

    private val finishSatisfactionRoundUseCase by lazy {
        val csvWriter = SatisfactionCsvWriter(applicationContext)
        val sync = SatisfactionOneDriveSync(csvWriter)
        val repo = FileSatisfactionLogRepository(csvWriter, sync)
        FinishSatisfactionRoundUseCase(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_satisfaction_individual)

        robot = Robot.getInstance()
        bindViews()
        setupBeds()
        setupButtons()

        speak("Selecciona primero la cama del paciente. Después iré hasta allí y realizaremos una encuesta corta de satisfacción.")
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
        runOnUiThread {
            if (!inSurvey) return@runOnUiThread

            val transcript = asrResult
                .lowercase(Locale.getDefault())
                .trim()

            if (transcript.isBlank()) {
                Toast.makeText(
                    this,
                    "No entendí. Intenta de nuevo o usa los botones.",
                    Toast.LENGTH_SHORT
                ).show()
                return@runOnUiThread
            }

            handleVoiceAnswer(transcript)
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
                textSize = 16f
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
                Toast.makeText(this, "Seleccione una cama.", Toast.LENGTH_LONG).show()
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

            val index = rgOptions.indexOfChild(findViewById(checkedId))
            val q = surveyQuestions[currentQuestionIndex]
            val optText = (findViewById<RadioButton>(checkedId)).text.toString()

            currentAnswers.add(
                SatisfactionAnswer(
                    questionId = q.id,
                    questionLabel = q.label,
                    optionIndex = index,
                    optionText = optText,
                    isRegulatoryKey = q.isRegulatoryKey
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

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        runOnUiThread {
            if (status.equals("complete", ignoreCase = true)) {
                val bed = selectedBed
                if (bed != null && location.equals(bed.locationName, ignoreCase = true)) {
                    startSurvey()
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

        speak("Estamos en la ${bed.label}. Le haré algunas preguntas cortas sobre la atención recibida. Puede responder por voz o tocando la pantalla.")
        showCurrentQuestion()
    }

    private fun showCurrentQuestion() {
        val q = surveyQuestions[currentQuestionIndex]
        tvQuestion.text = q.label
        rgOptions.removeAllViews()

        for (opt in q.options) {
            val rb = RadioButton(this).apply {
                text = opt
                textSize = 16f
            }
            rgOptions.addView(rb)
        }

        speak(q.textTts + " Puedes responder diciendo la opción o tocándola en la pantalla.")
    }

    private fun saveSessionAndFinish() {
        val bed = selectedBed ?: return
        val answers = currentAnswers.toList()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                finishSatisfactionRoundUseCase(
                    mode = SatisfactionMode.INDIVIDUAL,
                    bed = bed,
                    answers = answers
                )
            }

            speak("Gracias por sus respuestas. Esto nos ayuda a mejorar la calidad de la atención.")
            finish()
        }
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
        }

        if (index == null || index !in q.options.indices) {
            Toast.makeText(
                this,
                "No entendí la respuesta. Intenta de nuevo o usa la pantalla.",
                Toast.LENGTH_SHORT
            ).show()
            speak("No entendí la respuesta. Puedes intentar de nuevo, o tocar una opción en la pantalla.")
            return
        }

        val rb = rgOptions.getChildAt(index) as? RadioButton
        rb?.isChecked = true
        speak("Registré su respuesta: ${q.options[index]}. Puedes tocar siguiente para continuar.")
    }

    private fun mapLikert5(text: String): Int? {
        return when {
            text.contains("muy mala") || text.contains("muy insatisfecho") -> 0
            text.contains("mala") || text.contains("insatisfecho") -> 1
            text.contains("regular") || text.contains("ni satisfecho") -> 2
            text.contains("muy buena") || text.contains("muy satisfecho") -> 4
            text.contains("buena") || text.contains("satisfecho") -> 3
            else -> {
                Regex("""\d""").find(text)?.value?.toIntOrNull()?.let {
                    if (it in 1..5) it - 1 else null
                }
            }
        }
    }

    private fun mapRecommend4(text: String): Int? {
        return when {
            text.contains("definitivamente sí") ||
                    (text.contains("totalmente") && text.contains("sí")) -> 0

            text.contains("probablemente sí") ||
                    (text.contains("sí") && !text.contains("definitivamente")) -> 1

            text.contains("probablemente no") -> 2

            text.contains("definitivamente no") ||
                    (text.contains("no") && !text.contains("probablemente")) -> 3

            else -> null
        }
    }

    private fun speak(text: String) {
        robot.speak(TtsRequest.create(text, false))
    }
}