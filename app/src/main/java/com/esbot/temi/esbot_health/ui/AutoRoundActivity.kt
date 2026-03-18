package com.esbot.temi.esbot_health.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.sequence.OnSequencePlayStatusChangedListener
import com.esbot.temi.esbot_health.R
import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.education.EducationEvent
import com.esbot.temi.esbot_health.education.EducationLogStore
import com.esbot.temi.esbot_health.education.EducationTopic
import com.esbot.temi.esbot_health.education.HospitalConfig
import com.esbot.temi.esbot_health.education.SequencePlayer
import com.google.android.material.card.MaterialCardView

class AutoRoundActivity : AppCompatActivity(),
    OnGoToLocationStatusChangedListener,
    OnSequencePlayStatusChangedListener {

    private lateinit var robot: Robot
    private val handler = Handler(Looper.getMainLooper())

    private enum class ConfigStep { TOPIC, BEDS, RULES, SUMMARY, RUNNING }

    private var currentStep: ConfigStep = ConfigStep.TOPIC

    private var selectedTopic: EducationTopic? = null
    private val selectedBeds: MutableSet<BedInfo> = linkedSetOf()
    private var rules: RoundRules = RoundRules()

    private var currentConfig: RoundConfig? = null

    private var isRoundRunning: Boolean = false
    private var currentBedIndex: Int = -1
    private var currentBed: BedInfo? = null
    private var lastSequenceId: String? = null

    private var waitingAvailability: Boolean = false
    private var availabilityTimeoutRunnable: Runnable? = null

    private lateinit var tvRoundTitle: TextView
    private lateinit var tvRoundStep: TextView

    private lateinit var stepTopic: MaterialCardView
    private lateinit var stepBeds: MaterialCardView
    private lateinit var stepRules: MaterialCardView
    private lateinit var stepSummary: MaterialCardView
    private lateinit var panelRunning: MaterialCardView

    private lateinit var layoutTopicList: LinearLayout

    private lateinit var tvBedsFloor: TextView
    private lateinit var layoutBedList: LinearLayout
    private lateinit var btnBedsSelectAll: Button
    private lateinit var btnBedsClear: Button

    private lateinit var cbAskAvailability: CheckBox
    private lateinit var cbSkipIfNoResponse: CheckBox
    private lateinit var etSkipSeconds: EditText
    private lateinit var cbReturnToNursing: CheckBox
    private lateinit var cbAutoRepeatIfLow: CheckBox

    private lateinit var tvSummary: TextView

    private lateinit var tvRunningHeader: TextView
    private lateinit var tvRunningState: TextView
    private lateinit var panelAvailability: LinearLayout
    private lateinit var tvAvailabilityQuestion: TextView
    private lateinit var btnAvailYes: Button
    private lateinit var btnAvailNo: Button

    private lateinit var panelComprehension: LinearLayout
    private lateinit var rgComp: RadioGroup
    private lateinit var rbCompFull: RadioButton
    private lateinit var rbCompPartial: RadioButton
    private lateinit var rbCompNone: RadioButton
    private lateinit var etCompNotes: EditText
    private lateinit var cbCompReplay: CheckBox
    private lateinit var btnCompSave: Button

    private lateinit var btnPrevStep: Button
    private lateinit var btnNextStep: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_round)

        robot = Robot.getInstance()

        bindViews()
        setupTopicList()
        setupBedList()
        setupRulesDefaults()
        setupButtons()

        updateStepUI()
    }

    override fun onStart() {
        super.onStart()
        robot.addOnGoToLocationStatusChangedListener(this)
        robot.addOnSequencePlayStatusChangedListener(this)
    }

    override fun onStop() {
        robot.removeOnGoToLocationStatusChangedListener(this)
        robot.removeOnSequencePlayStatusChangedListener(this)
        super.onStop()
    }

    private fun bindViews() {
        tvRoundTitle = findViewById(R.id.tvRoundTitle)
        tvRoundStep = findViewById(R.id.tvRoundStep)

        stepTopic = findViewById(R.id.stepTopic)
        stepBeds = findViewById(R.id.stepBeds)
        stepRules = findViewById(R.id.stepRules)
        stepSummary = findViewById(R.id.stepSummary)
        panelRunning = findViewById(R.id.panelRunning)

        layoutTopicList = findViewById(R.id.layoutTopicList)

        tvBedsFloor = findViewById(R.id.tvBedsFloor)
        layoutBedList = findViewById(R.id.layoutBedList)
        btnBedsSelectAll = findViewById(R.id.btnBedsSelectAll)
        btnBedsClear = findViewById(R.id.btnBedsClear)

        cbAskAvailability = findViewById(R.id.cbAskAvailability)
        cbSkipIfNoResponse = findViewById(R.id.cbSkipIfNoResponse)
        etSkipSeconds = findViewById(R.id.etSkipSeconds)
        cbReturnToNursing = findViewById(R.id.cbReturnToNursing)
        cbAutoRepeatIfLow = findViewById(R.id.cbAutoRepeatIfLow)

        tvSummary = findViewById(R.id.tvSummary)

        tvRunningHeader = findViewById(R.id.tvRunningHeader)
        tvRunningState = findViewById(R.id.tvRunningState)
        panelAvailability = findViewById(R.id.panelAvailability)
        tvAvailabilityQuestion = findViewById(R.id.tvAvailabilityQuestion)
        btnAvailYes = findViewById(R.id.btnAvailYes)
        btnAvailNo = findViewById(R.id.btnAvailNo)

        panelComprehension = findViewById(R.id.panelComprehension)
        rgComp = findViewById(R.id.rgComp)
        rbCompFull = findViewById(R.id.rbCompFull)
        rbCompPartial = findViewById(R.id.rbCompPartial)
        rbCompNone = findViewById(R.id.rbCompNone)
        etCompNotes = findViewById(R.id.etCompNotes)
        cbCompReplay = findViewById(R.id.cbCompReplay)
        btnCompSave = findViewById(R.id.btnCompSave)

        btnPrevStep = findViewById(R.id.btnPrevStep)
        btnNextStep = findViewById(R.id.btnNextStep)

        tvRoundTitle.text = "Ronda automática de educación"
        tvBedsFloor.text = "Área / piso: ${HospitalConfig.FLOOR_NAME}"
    }

    private fun setupTopicList() {
        layoutTopicList.removeAllViews()
        val topics = EducationTopic.allTopics()
        topics.forEach { topic ->
            val radioButton = RadioButton(this).apply {
                text = topic.displayName
                textSize = 60f
                buttonDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_big
                )
                setOnClickListener {
                    selectedTopic = topic
                }
            }
            layoutTopicList.addView(radioButton)
        }
    }

    private fun setupBedList() {
        layoutBedList.removeAllViews()
        HospitalConfig.bedsMi.forEach { bed ->
            val checkBox = CheckBox(this).apply {
                text = bed.label
                textSize = 60f
                buttonDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_big
                )
                isChecked = true
                buttonTintList =
                    ContextCompat.getColorStateList(
                        context,
                        R.color.color_primary
                    )
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedBeds.add(bed)
                    } else {
                        selectedBeds.remove(bed)
                    }
                }
            }
            selectedBeds.add(bed)
            layoutBedList.addView(checkBox)
        }

        btnBedsSelectAll.setOnClickListener {
            for (i in 0 until layoutBedList.childCount) {
                val cb = layoutBedList.getChildAt(i) as? CheckBox ?: continue
                cb.isChecked = true
            }
        }

        btnBedsClear.setOnClickListener {
            for (i in 0 until layoutBedList.childCount) {
                val cb = layoutBedList.getChildAt(i) as? CheckBox ?: continue
                cb.isChecked = false
            }
        }
    }

    private fun setupRulesDefaults() {
        cbAskAvailability.isChecked = true
        cbSkipIfNoResponse.isChecked = true
        etSkipSeconds.setText("30")
        cbReturnToNursing.isChecked = true
        cbAutoRepeatIfLow.isChecked = false
    }

    private fun setupButtons() {
        btnPrevStep.setOnClickListener { onPrevStep() }
        btnNextStep.setOnClickListener { onNextStep() }

        btnAvailYes.setOnClickListener {
            cancelAvailabilityTimeout()
            waitingAvailability = false
            panelAvailability.visibility = View.GONE
            startEducationAtCurrentBed()
        }

        btnAvailNo.setOnClickListener {
            cancelAvailabilityTimeout()
            waitingAvailability = false
            panelAvailability.visibility = View.GONE
            markBedAsSkipped("Paciente no disponible")
            goToNextBed()
        }

        btnCompSave.setOnClickListener {
            onSaveComprehension()
        }
    }

    private fun onPrevStep() {
        when (currentStep) {
            ConfigStep.TOPIC -> {
                finish()
            }
            ConfigStep.BEDS -> {
                currentStep = ConfigStep.TOPIC
            }
            ConfigStep.RULES -> {
                currentStep = ConfigStep.BEDS
            }
            ConfigStep.SUMMARY -> {
                currentStep = ConfigStep.RULES
            }
            ConfigStep.RUNNING -> {
                return
            }
        }
        updateStepUI()
    }

    private fun onNextStep() {
        when (currentStep) {
            ConfigStep.TOPIC -> {
                if (!validateTopic()) return
                currentStep = ConfigStep.BEDS
                updateStepUI()
            }
            ConfigStep.BEDS -> {
                if (!validateBeds()) return
                currentStep = ConfigStep.RULES
                updateStepUI()
            }
            ConfigStep.RULES -> {
                readRulesFromUI()
                currentStep = ConfigStep.SUMMARY
                generateSummary()
                updateStepUI()
            }
            ConfigStep.SUMMARY -> {
                startRound()
            }
            ConfigStep.RUNNING -> {
                stopRound()
            }
        }
    }

    private fun validateTopic(): Boolean {
        if (selectedTopic == null) {
            Toast.makeText(this, "Selecciona un tema para la ronda.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun validateBeds(): Boolean {
        if (selectedBeds.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una cama para la ronda.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun readRulesFromUI() {
        val skipSeconds = etSkipSeconds.text?.toString()?.toIntOrNull() ?: 30
        rules = RoundRules(
            askAvailability = cbAskAvailability.isChecked,
            skipIfNoResponse = cbSkipIfNoResponse.isChecked,
            skipTimeoutSeconds = skipSeconds.coerceAtLeast(5),
            returnToNursingAtEnd = cbReturnToNursing.isChecked,
            autoRepeatIfLow = cbAutoRepeatIfLow.isChecked
        )
    }

    private fun generateSummary() {
        val topic = selectedTopic
        if (topic == null) {
            tvSummary.text = "Config incompleta."
            return
        }
        val bedsText = selectedBeds.joinToString(separator = "\n") { "- ${it.label}" }
        val sb = StringBuilder()
        sb.appendLine("Tema: ${topic.displayName}")
        sb.appendLine("Área: ${HospitalConfig.FLOOR_NAME}")
        sb.appendLine("Camas:")
        sb.appendLine(bedsText)
        sb.appendLine()
        sb.appendLine("Reglas:")
        sb.appendLine("- Preguntar disponibilidad: ${if (rules.askAvailability) "Sí" else "No"}")
        sb.appendLine("- Saltar si no responde en ${if (rules.skipIfNoResponse) rules.skipTimeoutSeconds else 0} s")
        sb.appendLine("- Volver a Enfermería al finalizar: ${if (rules.returnToNursingAtEnd) "Sí" else "No"}")
        sb.appendLine("- Repetir si comprensión baja: ${if (rules.autoRepeatIfLow) "Sí" else "No"}")

        tvSummary.text = sb.toString()
    }

    private fun updateStepUI() {
        stepTopic.visibility = View.GONE
        stepBeds.visibility = View.GONE
        stepRules.visibility = View.GONE
        stepSummary.visibility = View.GONE
        panelRunning.visibility = View.GONE

        when (currentStep) {
            ConfigStep.TOPIC -> {
                tvRoundStep.text = "Paso 1 de 4: Selecciona el tema"
                stepTopic.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnNextStep.text = "Siguiente"
            }
            ConfigStep.BEDS -> {
                tvRoundStep.text = "Paso 2 de 4: Selecciona las camas"
                stepBeds.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnNextStep.text = "Siguiente"
            }
            ConfigStep.RULES -> {
                tvRoundStep.text = "Paso 3 de 4: Define las reglas de la ronda"
                stepRules.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnNextStep.text = "Siguiente"
            }
            ConfigStep.SUMMARY -> {
                tvRoundStep.text = "Paso 4 de 4: Revisa y confirma"
                stepSummary.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnNextStep.text = "Iniciar ronda"
            }
            ConfigStep.RUNNING -> {
                tvRoundStep.text = "Ronda en curso"
                panelRunning.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnPrevStep.isEnabled = false
                btnNextStep.text = "Detener ronda"
            }
        }
    }

    private fun startRound() {
        val topic = selectedTopic ?: return
        if (selectedBeds.isEmpty()) {
            Toast.makeText(this, "No hay camas seleccionadas.", Toast.LENGTH_LONG).show()
            return
        }

        currentConfig = RoundConfig(
            topic = topic,
            beds = selectedBeds.toList(),
            rules = rules
        )

        isRoundRunning = true
        currentBedIndex = -1
        currentStep = ConfigStep.RUNNING
        updateStepUI()

        speak("Iniciando ronda de educación sobre ${topic.displayName} en ${HospitalConfig.FLOOR_NAME}.")
        goToNextBed()
    }

    private fun stopRound() {
        isRoundRunning = false
        cancelAvailabilityTimeout()
        speak("He detenido la ronda de educación.")
        finish()
    }

    private fun goToNextBed() {
        val cfg = currentConfig ?: return
        cancelAvailabilityTimeout()
        panelAvailability.visibility = View.GONE
        panelComprehension.visibility = View.GONE

        currentBedIndex += 1
        if (currentBedIndex >= cfg.beds.size) {
            finishRound()
            return
        }

        currentBed = cfg.beds[currentBedIndex]
        val bed = currentBed!!

        tvRunningHeader.text = "Cama ${currentBedIndex + 1} de ${cfg.beds.size}: ${bed.label}"
        tvRunningState.text = "Desplazándome hacia la cama."

        try {
            robot.goTo(bed.locationName)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "No pude ir a ${bed.label}", Toast.LENGTH_LONG).show()
            goToNextBed()
        }
    }

    private fun finishRound() {
        isRoundRunning = false
        cancelAvailabilityTimeout()
        tvRunningHeader.text = "Ronda finalizada"
        tvRunningState.text = "He terminado todas las camas configuradas."

        val cfg = currentConfig

        if (cfg?.rules?.returnToNursingAtEnd == true) {
            speak("He terminado la ronda. Voy a Enfermería.")
            try {
                robot.goTo(HospitalConfig.NURSING_LOCATION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            speak("He terminado la ronda de educación.")
        }

        btnPrevStep.isEnabled = true
        btnNextStep.text = "Cerrar"
        btnNextStep.setOnClickListener {
            finish()
        }
    }

    private fun onArrivedAtCurrentBed() {
        val cfg = currentConfig ?: return
        val bed = currentBed ?: return

        tvRunningState.text = "En ${bed.label}."

        if (cfg.rules.askAvailability) {
            showAvailabilityPanel()
        } else {
            startEducationAtCurrentBed()
        }
    }

    private fun showAvailabilityPanel() {
        val cfg = currentConfig ?: return
        val topic = cfg.topic
        waitingAvailability = true
        panelAvailability.visibility = View.VISIBLE
        panelComprehension.visibility = View.GONE

        tvAvailabilityQuestion.text =
            "Hola, soy Temi, el robot de la clínica.\n¿Podemos hablar unos minutos sobre:\n${topic.displayName}?"

        tvRunningState.text = "Esperando confirmación del paciente."
        tvRunningState.textSize = 28f

        if (cfg.rules.skipIfNoResponse) {
            val timeoutMs = cfg.rules.skipTimeoutSeconds * 1000L
            availabilityTimeoutRunnable = Runnable {
                if (!waitingAvailability) return@Runnable
                waitingAvailability = false
                panelAvailability.visibility = View.GONE
                markBedAsSkipped("Sin respuesta del paciente")
                tvRunningState.text = "Paciente no respondió. Pasando a la siguiente cama."
                goToNextBed()
            }
            handler.postDelayed(availabilityTimeoutRunnable!!, timeoutMs)
        }
    }

    private fun cancelAvailabilityTimeout() {
        val r = availabilityTimeoutRunnable ?: return
        handler.removeCallbacks(r)
        availabilityTimeoutRunnable = null
    }

    private fun startEducationAtCurrentBed() {
        val cfg = currentConfig ?: return
        val topic = cfg.topic
        val bed = currentBed ?: return

        tvRunningState.text = "Mostrando educación sobre ${topic.displayName}."
        panelComprehension.visibility = View.GONE

        SequencePlayer.debugLogAllSequences(robot)

        val seqId = SequencePlayer.playTopicSequence(robot, topic)

        if (seqId == null) {
            Toast.makeText(
                this,
                "No se pudo iniciar la secuencia para ${topic.displayName} en ${bed.label}.",
                Toast.LENGTH_LONG
            ).show()
            markBedAsSkipped("No se encontró o no se pudo reproducir la secuencia")
            goToNextBed()
        } else {
            lastSequenceId = seqId

            // Failsafe: si Temi no avisa que terminó
            handler.postDelayed({
                if (
                    isRoundRunning &&
                    lastSequenceId == seqId &&
                    panelComprehension.visibility != View.VISIBLE
                ) {
                    lastSequenceId = null
                    showComprehensionPanel()
                }
            }, 0) // 2 minutos (ajusta según duración real)
        }
    }

    private fun showComprehensionPanel() {
        panelAvailability.visibility = View.GONE
        panelComprehension.visibility = View.VISIBLE
        rgComp.clearCheck()
        etCompNotes.setText("")
        cbCompReplay.isChecked = false

        tvRunningState.text = "Registra qué tanto se entendió la explicación."
        tvRunningState.textSize = 45f
        speak("He terminado la explicación. En la pantalla puedes indicar qué tanto se entendió.")

    }

    private fun onSaveComprehension() {
        val cfg = currentConfig ?: return
        val bed = currentBed ?: return
        val topic = cfg.topic

        val level = when (rgComp.checkedRadioButtonId) {
            R.id.rbCompFull -> "FULL"
            R.id.rbCompPartial -> "PARTIAL"
            R.id.rbCompNone -> "NONE"
            else -> {
                Toast.makeText(
                    this,
                    "Selecciona un nivel de comprensión.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        val notes = etCompNotes.text?.toString()?.trim().orEmpty()
        val needsReplay = cbCompReplay.isChecked &&
                (level == "PARTIAL" || level == "NONE") &&
                cfg.rules.autoRepeatIfLow

        val event = EducationEvent(
            timestampMillis = System.currentTimeMillis(),
            topicId = topic.id,
            topicName = topic.displayName,
            bedId = bed.id,
            bedLabel = bed.label,
            mode = "AUTO_ROUND",
            comprehensionLevel = level
//            needsReplay = needsReplay
//            note = notes
        )
        EducationLogStore.appendEvent(this, event)

        Toast.makeText(
            this,
            "Comprensión registrada para ${bed.label}.",
            Toast.LENGTH_SHORT
        ).show()

        speak(
            when (level) {
                "FULL" -> "Perfecto, he registrado que la explicación se entendió bien."
                "PARTIAL" -> "He registrado que se entendió solo parcialmente. El equipo de enfermería puede reforzar esta información."
                else -> "He registrado que la explicación no se entendió. Informaré al equipo de enfermería."
            }
        )


        if (needsReplay) {
            speak("Voy a repetir la explicación para reforzar la información.")
            startEducationAtCurrentBed()
        } else {
            panelComprehension.visibility = View.GONE
            goToNextBed()
        }
    }

    private fun markBedAsSkipped(reason: String) {
        val cfg = currentConfig ?: return
        val bed = currentBed ?: return
        val topic = cfg.topic

        val event = EducationEvent(
            timestampMillis = System.currentTimeMillis(),
            topicId = topic.id,
            topicName = topic.displayName,
            bedId = bed.id,
            bedLabel = bed.label,
            mode = "AUTO_ROUND",
            comprehensionLevel = "SKIPPED"
//            needsReplay = false
//            note = reason
        )
        EducationLogStore.appendEvent(this, event)
    }

    private fun speak(text: String) {
        val req = TtsRequest.create(text, false)
        robot.speak(req)
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        if (!isRoundRunning) return
        val bed = currentBed ?: return

        if (location != bed.locationName) return

        if (status == "complete") {
            runOnUiThread { onArrivedAtCurrentBed() }
        } else if (status == "error" || status == "abort") {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Problema al ir a ${bed.label}: $description",
                    Toast.LENGTH_LONG
                ).show()
                markBedAsSkipped("Error de navegación: $description")
                goToNextBed()
            }
        }
    }

    override fun onSequencePlayStatusChanged(status: Int, sequenceId: String?) {
        if (!isRoundRunning) return
        val id = sequenceId ?: return
        if (id != lastSequenceId) return

        if (status == OnSequencePlayStatusChangedListener.IDLE) {
            lastSequenceId = null
            runOnUiThread {
                showComprehensionPanel()
            }
        }
    }


    data class RoundRules(
        val askAvailability: Boolean = true,
        val skipIfNoResponse: Boolean = true,
        val skipTimeoutSeconds: Int = 5,
        val returnToNursingAtEnd: Boolean = true,
        val autoRepeatIfLow: Boolean = false
    )

    data class RoundConfig(
        val topic: EducationTopic,
        val beds: List<BedInfo>,
        val rules: RoundRules
    )
}