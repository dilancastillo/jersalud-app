package com.esbot.temi.esbot_health.satisfaction

enum class SatisfactionQuestionType {
    LIKERT_5,
    RECOMMEND_4,
    SINGLE_CHOICE_LIST
}

data class SatisfactionQuestion(
    val id: String,
    val label: String,
    val textTts: String,
    val type: SatisfactionQuestionType,
    val options: List<String>,
    val isRegulatoryKey: Boolean
)

data class SatisfactionAnswer(
    val questionId: String,
    val questionLabel: String,
    val optionIndex: Int,
    val optionText: String
)

object SatisfactionSurvey {
    fun shortSurvey(): List<SatisfactionQuestion> {
        val likert5 = listOf(
            "Muy insatisfecho",
            "Insatisfecho",
            "Ni satisfecho ni insatisfecho",
            "Satisfecho",
            "Muy satisfecho"
        )

        return listOf(
            SatisfactionQuestion(
                id = "P001_SERVICE_EVALUATED",
                label = "Servicio evaluado",
                textTts = "Por favor indique el servicio que desea evaluar en Instituto Médico Oncológico IMO.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST, // tipo no importa mucho; 5 opciones o más
                options = listOf(
                    "ECIS-M (Visita de Caracterización familiar)",
                    "Medicina Laboral",
                    "Terapia Física",
                    "Medicina General",
                    "Enfermería",
                    "Odontología",
                    "Farmacia",
                    "Toma de muestra de laboratorio",
                    "Admisión (activación y asignación de citas)",
                    "Medicina Especializada",
                    "Trabajo Social",
                    "Psicología",
                    "Administrativos"
                ),
                isRegulatoryKey = false
            ),
            SatisfactionQuestion(
                id = "P002_RECOMMEND",
                label = "¿Recomendaría a sus familiares y amigos Instituto Médico Oncológico IMO?",
                textTts = "¿Recomendaría a sus familiares y amigos Instituto Médico Oncológico IMO?",
                type = SatisfactionQuestionType.RECOMMEND_4,
                options = listOf(
                    "Definitivamente Sí",
                    "Probablemente Sí",
                    "Probablemente No",
                    "Definitivamente No"
                ),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P003_GLOBAL_EXPERIENCE",
                label = "¿Cómo calificaría su experiencia global respecto a ?",
                textTts = "¿Cómo calificaría su experiencia global respecto ?",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf(
                    "Muy Buena",
                    "Buena",
                    "Regular",
                    "Mala",
                    "Muy mala"
                ),
                isRegulatoryKey = true
            )
            /*SatisfactionQuestion(
                id = "P314_GLOBAL_EXPERIENCE",
                label = "Experiencia global con la atención",
                textTts = "En general, ¿cómo calificaría su experiencia global con los servicios de salud recibidos en esta clínica?",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf(
                    "Muy mala",
                    "Mala",
                    "Regular",
                    "Buena",
                    "Muy buena"
                ),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P315_RECOMMEND",
                label = "¿Recomendaría esta clínica?",
                textTts = "¿Recomendaría esta clínica o hospital a sus familiares y amigos?",
                type = SatisfactionQuestionType.RECOMMEND_4,
                options = listOf(
                    "Definitivamente sí",
                    "Probablemente sí",
                    "Probablemente no",
                    "Definitivamente no"
                ),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "NURSING_TREATMENT",
                label = "Trato del personal de enfermería",
                textTts = "¿Qué tan satisfecho está con el trato y la amabilidad del personal de enfermería?",
                type = SatisfactionQuestionType.LIKERT_5,
                options = likert5,
                isRegulatoryKey = false
            ),

            SatisfactionQuestion(
                id = "INFO_CLARITY",
                label = "Claridad de la información",
                textTts = "¿Qué tan satisfecho está con la claridad de la información sobre su enfermedad y tratamiento?",
                type = SatisfactionQuestionType.LIKERT_5,
                options = likert5,
                isRegulatoryKey = false
            ),

            SatisfactionQuestion(
                id = "CLEANLINESS_REST",
                label = "Limpieza y posibilidad de descanso",
                textTts = "¿Qué tan satisfecho está con la limpieza de la habitación y la posibilidad de descansar, por ejemplo el ruido?",
                type = SatisfactionQuestionType.LIKERT_5,
                options = likert5,
                isRegulatoryKey = false
            )*/
        )
    }
}