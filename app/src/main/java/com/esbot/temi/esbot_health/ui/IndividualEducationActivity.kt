package com.esbot.temi.esbot_health.ui

import android.os.Bundle
import android.os.Handler
import android.util.Log
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

class IndividualEducationActivity : AppCompatActivity(),
    OnGoToLocationStatusChangedListener,
    OnSequencePlayStatusChangedListener {

    private lateinit var robot: Robot

    private enum class Step { TOPIC, BED, RUNNING }

    private var currentStep: Step = Step.TOPIC

    private var selectedTopic: EducationTopic? = null
    private var selectedBed: BedInfo? = null

    private var isSessionRunning: Boolean = false
    private var lastSequenceId: String? = null

    private lateinit var tvTitle: TextView
    private lateinit var tvStep: TextView

    private lateinit var stepTopic: LinearLayout
    private lateinit var stepBed: LinearLayout
    private lateinit var panelRunning: LinearLayout

    private lateinit var layoutTopicList: LinearLayout

    private lateinit var tvBedFloor: TextView
    private lateinit var rgBedList: RadioGroup

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
//    private lateinit var etCompNotes: EditText
//    private lateinit var cbCompReplay: CheckBox
    private lateinit var btnCompSave: Button

    private lateinit var btnPrevStep: Button
    private lateinit var btnNextStep: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_education)

        robot = Robot.getInstance()

        bindViews()
        setupTopicList()
        setupBedList()
        setupButtonBar()

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
        tvTitle = findViewById(R.id.tvIndTitle)
        tvStep = findViewById(R.id.tvIndStep)

        stepTopic = findViewById(R.id.stepIndTopic)
        stepBed = findViewById(R.id.stepIndBed)
        panelRunning = findViewById(R.id.panelIndRunning)

        layoutTopicList = findViewById(R.id.layoutIndTopicList)

        tvBedFloor = findViewById(R.id.tvIndBedFloor)
        rgBedList = findViewById(R.id.rgIndBedList)

        tvRunningHeader = findViewById(R.id.tvIndRunningHeader)
        tvRunningState = findViewById(R.id.tvIndRunningState)
        panelAvailability = findViewById(R.id.panelIndAvailability)
        tvAvailabilityQuestion = findViewById(R.id.tvIndAvailabilityQuestion)
        btnAvailYes = findViewById(R.id.btnIndAvailYes)
        btnAvailNo = findViewById(R.id.btnIndAvailNo)

        panelComprehension = findViewById(R.id.panelIndComprehension)
        rgComp = findViewById(R.id.rgIndComp)
        rbCompFull = findViewById(R.id.rbIndCompFull)
        rbCompPartial = findViewById(R.id.rbIndCompPartial)
        rbCompNone = findViewById(R.id.rbIndCompNone)
//        etCompNotes = findViewById(R.id.etIndCompNotes)
//        cbCompReplay = findViewById(R.id.cbIndCompReplay)
        btnCompSave = findViewById(R.id.btnIndCompSave)

        btnPrevStep = findViewById(R.id.btnIndPrevStep)
        btnNextStep = findViewById(R.id.btnIndNextStep)

        tvTitle.text = "Sesión individual de educación"
        tvBedFloor.text = "Área / piso: ${HospitalConfig.FLOOR_NAME}"
    }

    private fun setupTopicList() {
        layoutTopicList.removeAllViews()
        val topics = EducationTopic.allTopics()
        topics.forEach { topic ->
            val radio = RadioButton(this).apply {
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
            layoutTopicList.addView(radio)
        }
    }

    private fun setupBedList() {
        rgBedList.removeAllViews()
        HospitalConfig.bedsMi.forEachIndexed { index, bed ->
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = bed.label
                textSize = 60f
                buttonDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.radio_big
                )
                if (index == 0) {
                    isChecked = true
                    selectedBed = bed
                }
            }
            rgBedList.addView(radio)

            radio.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedBed = bed
                }
            }
        }
    }

    private fun setupButtonBar() {
        btnPrevStep.setOnClickListener {
            when (currentStep) {
                Step.TOPIC -> finish()
                Step.BED -> {
                    currentStep = Step.TOPIC
                    updateStepUI()
                }
                Step.RUNNING -> {
                    stopSession()
                }
            }
        }

        btnNextStep.setOnClickListener {
            when (currentStep) {
                Step.TOPIC -> {
                    if (!validateTopic()) return@setOnClickListener
                    currentStep = Step.BED
                    updateStepUI()
                }
                Step.BED -> {
                    if (!validateBed()) return@setOnClickListener
                    startSession()
                }
                Step.RUNNING -> {
                    stopSession()
                }
            }
        }

        btnAvailYes.setOnClickListener {
            panelAvailability.visibility = View.GONE
            startEducationAtBed()
        }

        btnAvailNo.setOnClickListener {
            markNotAvailable("Paciente/familia indicó que no es buen momento")
            speak("Perfecto, informaré a enfermería que no fue posible hacer la explicación ahora.")
            stopSession()
        }

        btnCompSave.setOnClickListener {
            onSaveComprehension()
        }
    }

    private fun validateTopic(): Boolean {
        if (selectedTopic == null) {
            Toast.makeText(this, "Selecciona un tema de educación.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun validateBed(): Boolean {
        if (selectedBed == null) {
            Toast.makeText(this, "Selecciona una cama.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun updateStepUI() {
        stepTopic.visibility = View.GONE
        stepBed.visibility = View.GONE
        panelRunning.visibility = View.GONE

        when (currentStep) {
            Step.TOPIC -> {
                tvStep.text = "Paso 1 de 2: Selecciona el tema"
                stepTopic.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnNextStep.text = "Siguiente"
            }
            Step.BED -> {
                tvStep.text = "Paso 2 de 2: Selecciona la cama"
                stepBed.visibility = View.VISIBLE
                btnPrevStep.text = "Atrás"
                btnNextStep.text = "Iniciar sesión"
            }
            Step.RUNNING -> {
                tvStep.text = "Sesión en curso"
                panelRunning.visibility = View.VISIBLE
                btnPrevStep.text = "Cancelar"
                btnNextStep.text = "Finalizar"
            }
        }
    }

    private fun startSession() {
        val topic = selectedTopic ?: return
        val bed = selectedBed ?: return

        isSessionRunning = true
        currentStep = Step.RUNNING
        updateStepUI()

        tvRunningHeader.text = "Tema: ${topic.displayName}"
        tvRunningState.text = "Desplazándome hacia ${bed.label}."

        speak(
            "Voy a la ${bed.label} para hablar unos minutos sobre ${topic.displayName}."
        )

        try {
            robot.goTo(bed.locationName)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "No pude ir a ${bed.label}.",
                Toast.LENGTH_LONG
            ).show()
            stopSession()
        }
    }

    private fun stopSession() {
        isSessionRunning = false
        panelAvailability.visibility = View.GONE
        panelComprehension.visibility = View.GONE
        tvRunningState.text = "Sesión finalizada."
        speak("He terminado la sesión de educación.")
        robot.goTo("enfermeria")
        finish()
    }

//    private fun onArrivedAtBed() {
//        val topic = selectedTopic ?: return
//        val bed = selectedBed ?: return
//
//        tvRunningState.text = "En ${bed.label}."
//        panelAvailability.visibility = View.VISIBLE
//        panelComprehension.visibility = View.GONE
//
//        tvAvailabilityQuestion.text =
//            "Hola, soy Temi, el robot de la clínica.\n" +
//                    "¿Podemos hablar unos minutos sobre:\n${topic.displayName}?"
//        speak(
//            "Hola, soy Temi, el robot de la clínica. " +
//                    "¿Podemos hablar unos minutos sobre ${topic.displayName}?"
//        )
//    }

    private fun startEducationAtBed() {
        val topic = selectedTopic ?: return
        val bed = selectedBed ?: return

        panelAvailability.visibility = View.GONE
        panelComprehension.visibility = View.GONE

        tvRunningState.text = "Mostrando educación sobre ${topic.displayName}."
        speak("Voy a explicarte ${topic.displayName}.")

        SequencePlayer.debugLogAllSequences(robot)

        val seqId = SequencePlayer.playTopicSequence(robot, topic)
        if (seqId == null) {
            Toast.makeText(
                this,
                "No se pudo reproducir la secuencia para ${topic.displayName}.",
                Toast.LENGTH_LONG
            ).show()
            markNotAvailable("No se encontró o no se pudo reproducir la secuencia")
            stopSession()
        } else {
            lastSequenceId = seqId
//            Handler(mainLooper).postDelayed({
//                if (isSessionRunning && lastSequenceId == seqId && panelComprehension.visibility != View.VISIBLE) {
//                    lastSequenceId = null
//                    showComprehensionPanel()
//                }
//            }, 0)
        }
    }

    private fun showComprehensionPanel() {
        panelAvailability.visibility = View.GONE
        panelComprehension.visibility = View.VISIBLE
        btnPrevStep.visibility = View.GONE
        btnNextStep.visibility = View.GONE
        rgComp.clearCheck()
//        etCompNotes.setText("")
//        cbCompReplay.isChecked = false
        robot.cancelAllTtsRequests()
        tvRunningState.text = "Registra ¿Qué tanto se entendió la explicación.?"
        tvRunningState.textSize = 45f
        Handler(mainLooper).postDelayed({
            speak(
                "He terminado la explicación. " +
                        "En la pantalla puedes indicar qué tanto se entendió y anotar dudas."
            )
        }, 2000)
    }

    private fun onSaveComprehension() {
        val topic = selectedTopic ?: return
        val bed = selectedBed ?: return

        val level = when (rgComp.checkedRadioButtonId) {
            R.id.rbIndCompFull -> "FULL"
            R.id.rbIndCompPartial -> "PARTIAL"
            R.id.rbIndCompNone -> "NONE"
            else -> {
                Toast.makeText(
                    this,
                    "Selecciona un nivel de comprensión.",
                    Toast.LENGTH_SHORT
                ).show()
                speak("Selecciona un nivel de comprensión")
                return
            }
        }

//        val notes = etCompNotes.text?.toString()?.trim().orEmpty()
//        val needsReplay = cbCompReplay.isChecked &&
//                (level == "PARTIAL" || level == "NONE")

        val event = EducationEvent(
            timestampMillis = System.currentTimeMillis(),
            topicId = topic.id,
            topicName = topic.displayName,
            bedId = bed.id,
            bedLabel = bed.label,
            mode = "INDIVIDUAL",
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
                "PARTIAL" -> "He registrado que la explicación se entendió parcialmente. El equipo de enfermería puede reforzar la información."
                else -> "He registrado que la explicación no se entendió. El equipo de enfermería puede ayudarte a resolver tus dudas."
            }
        )

        Handler(mainLooper).postDelayed({
//            if (needsReplay) {
//                speak("Voy a repetir la explicación para reforzar la información.")
//                startEducationAtBed()
//            } else {
                stopSession()
           // }
        }, 7500)
    }

    private fun markNotAvailable(reason: String) {
        val topic = selectedTopic ?: return
        val bed = selectedBed ?: return

        val event = EducationEvent(
            timestampMillis = System.currentTimeMillis(),
            topicId = topic.id,
            topicName = topic.displayName,
            bedId = bed.id,
            bedLabel = bed.label,
            mode = "INDIVIDUAL",
            comprehensionLevel = "NOT_AVAILABLE"
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
        if (!isSessionRunning) return
        val bed = selectedBed ?: return

        if (location != bed.locationName) return

        if (status == "complete") {
            runOnUiThread {
                startEducationAtBed()
            }
        } else if (status == "error" || status == "abort") {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Problema al ir a ${bed.label}: $description",
                    Toast.LENGTH_LONG
                ).show()
                markNotAvailable("Error de navegación: $description")
                stopSession()
            }
        }
    }

    override fun onSequencePlayStatusChanged(status: Int, sequenceId: String?) {
        Log.d(
            "TEMI_SEQ",
            "status=$status sequenceId=$sequenceId lastSequenceId=$lastSequenceId"
        )
        if (!isSessionRunning) return

        if (status == 0) {
            isSessionRunning = false
            lastSequenceId = null

            runOnUiThread {
                showComprehensionPanel()
            }
        }
    }
}