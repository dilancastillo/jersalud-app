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
                id = "P001",
                label = "¿Cómo calificaría su experiencia global respecto a los servicios de salud que recibió en IMO - Instituto Médico Oncológico S.A.S.?",
                textTts = "¿Cómo calificaría su experiencia global respecto a los servicios de salud que recibió en IMO - Instituto Médico Oncológico S.A.S.? Responda diciendo Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P002",
                label = "¿Cómo calificaría su experiencia frente al trato respetuoso por parte del personal médico y demás colaboradores de IMO - Instituto Médico Oncológico S.A.S.?",
                textTts = "¿Cómo calificaría su experiencia frente al trato respetuoso por parte del personal médico y demás colaboradores de IMO - Instituto Médico Oncológico S.A.S.? Responda diciendo Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P003",
                label = "¿Cómo calificaría su experiencia frente a la información brindada por parte del personal médico y demás colaboradores de IMO - Instituto Médico Oncológico S.A.S.?",
                textTts = "¿Cómo calificaría su experiencia frente a la información brindada por parte del personal médico y demás colaboradores de IMO - Instituto Médico Oncológico S.A.S.? Responda diciendo Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P004",
                label = "¿Cómo calificaría su experiencia en relación con las condiciones de privacidad para su atención, incluida la discreción y confidencialidad del personal?",
                textTts = "¿Cómo calificaría su experiencia en relación con las condiciones de privacidad para su atención, incluida la discreción y confidencialidad del personal? Responda diciendo Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P005",
                label = "¿Cómo calificaría la limpieza y aseo de las instalaciones?",
                textTts = "¿Cómo calificaría la limpieza y aseo de las instalaciones? Responda diciendo Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P006",
                label = "¿Cómo calificaría su experiencia frente a la comodidad de las instalaciones en IMO - Instituto Médico Oncológico?",
                textTts = "¿Cómo calificaría su experiencia frente a la comodidad de las instalaciones en IMO - Instituto Médico Oncológico? Responda diciendo Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P007",
                label = "¿Le informaron los Derechos y Deberes que usted tiene como usuario?",
                textTts = "¿Le informaron los Derechos y Deberes que usted tiene como usuario? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P008",
                label = "Por favor, ¿Nos podría dar dos (2) ejemplos de derechos y explicarlos con sus propias palabras?",
                textTts = "Por favor, ¿Nos podría dar dos (2) ejemplos de derechos y explicarlos con sus propias palabras? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = false
            ),

            SatisfactionQuestion(
                id = "P009",
                label = "¿Nos podría dar dos (2) ejemplos de deberes y explicarlos con sus propias palabras?",
                textTts = "¿Nos podría dar dos (2) ejemplos de deberes y explicarlos con sus propias palabras? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = false
            ),

            SatisfactionQuestion(
                id = "P010",
                label = "¿Considera usted que está siendo atendido en una institución humanizada?",
                textTts = "¿Considera usted que está siendo atendido en una institución humanizada? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P011",
                label = "¿El profesional que lo atendió se presentó por su nombre?",
                textTts = "¿El profesional que lo atendió se presentó por su nombre? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P012",
                label = "¿El profesional que lo atendió lo llamó por su nombre?",
                textTts = "¿El profesional que lo atendió lo llamó por su nombre? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P013",
                label = "¿El profesional le habló claramente acerca de su condición de salud?",
                textTts = "¿El profesional le habló claramente acerca de su condición de salud? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P014",
                label = "Si le ordenó exámenes o procedimientos, ¿Le fueron explicados?",
                textTts = "Si le ordenó exámenes o procedimientos, ¿Le fueron explicados? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P015",
                label = "Si le formuló medicamentos ¿Le explicó para qué eran y cómo usarlos?",
                textTts = "Si le formuló medicamentos ¿Le explicó para qué eran y cómo usarlos? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P016",
                label = "¿Fue usted informado sobre aspectos relacionados con su atención? (toma de laboratorios, recomendaciones médicas)",
                textTts = "¿Fue usted informado sobre aspectos relacionados con su atención? (toma de laboratorios, recomendaciones médicas) Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P017",
                label = "¿Fue usted informado sobre aspectos administrativos (autorizaciones, próximo control)",
                textTts = "¿Fue usted informado sobre aspectos administrativos (autorizaciones, próximo control) Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P018",
                label = "En caso una emergencia en IMO, ¿Quién nos dará la orden de evacuación?",
                textTts = "En caso una emergencia en IMO, ¿Quién nos dará la orden de evacuación? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P019",
                label = "¿Podría indicarnos en este momento la ruta de evacuación en caso de una emergencia?",
                textTts = "¿Podría indicarnos en este momento la ruta de evacuación en caso de una emergencia? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P020",
                label = "¿Qué es lo más importante en caso de escuchar la alarma sonora de emergencia?",
                textTts = "¿Qué es lo más importante en caso de escuchar la alarma sonora de emergencia? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P021",
                label = "¿Le fue indicado que en caso de evacuación será llevado a un punto seguro hasta que cese la emergencia?",
                textTts = "¿Le fue indicado que en caso de evacuación será llevado a un punto seguro hasta que cese la emergencia? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P022",
                label = "En caso de emergencia, si usted estuviera en el segundo piso, ¿utilizaría el ascensor?",
                textTts = "En caso de emergencia, si usted estuviera en el segundo piso, ¿utilizaría el ascensor? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P023",
                label = "¿Le fue informado por parte del personal como se debe disponer los residuos en cada caneca según el color? ¿Podría explicarnos?",
                textTts = "¿Le fue informado por parte del personal como se debe disponer los residuos en cada caneca según el color? ¿Podría explicarnos? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P024",
                label = "¿Le explicaron la importancia de segregar correctamente los residuos para mantener un ambiente sano libre de contaminación?",
                textTts = "¿Le explicaron la importancia de segregar correctamente los residuos para mantener un ambiente sano libre de contaminación? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P025",
                label = "¿Sabe dónde manifestar una petición, queja, reclamo, sugerencia o felicitación?",
                textTts = "¿Sabe dónde manifestar una petición, queja, reclamo, sugerencia o felicitación? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P026",
                label = "¿Considera que hubo alguna barrera de acceso para recibir el servicio en IMO - Instituto Médico Oncológico?",
                textTts = "¿Considera que hubo alguna barrera de acceso para recibir el servicio en IMO - Instituto Médico Oncológico? Responda diciendo sí aplica o no aplica.",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Sí aplica", "No aplica"),
                isRegulatoryKey = true
            ),

            SatisfactionQuestion(
                id = "P027",
                label = "¿Recomendaría a sus familiares y amigos a IMO - Instituto Médico Oncológico S.A.S.?",
                textTts = "¿Recomendaría a sus familiares y amigos a IMO - Instituto Médico Oncológico S.A.S.? Responde por voz diciendo, Definitivamente sí, Probablemente sí, Probablemente no, Definitivamente no",
                type = SatisfactionQuestionType.RECOMMEND_4,
                options = listOf(
                    "Definitivamente sí",
                    "Probablemente sí",
                    "Probablemente no",
                    "Definitivamente no"
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