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
                label = "Experiencia global en servicios de salud",
                textTts = "¿Cómo calificaría su experiencia global respecto a los servicios de salud que recibió en IMO - Instituto Médico Oncológico S.A.S.? Opciones: Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P002",
                label = "Trato respetuoso del personal",
                textTts = "¿Cómo calificaría su experiencia frente al trato respetuoso por parte del personal médico y demás colaboradores de IMO - Instituto Médico Oncológico S.A.S.? Opciones: Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P003",
                label = "Información brindada por el personal",
                textTts = "¿Cómo calificaría su experiencia frente a la información brindada por parte del personal médico y demás colaboradores de IMO - Instituto Médico Oncológico S.A.S.? Opciones: Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P004",
                label = "Privacidad en la atención",
                textTts = "¿Cómo calificaría su experiencia en relación con las condiciones de privacidad para su atención, incluida la discreción y confidencialidad del personal? Opciones: Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P005",
                label = "Limpieza y aseo de las instalaciones",
                textTts = "¿Cómo calificaría la limpieza y aseo de las instalaciones? Opciones: Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P006",
                label = "Comodidad de las instalaciones",
                textTts = "¿Cómo calificaría su experiencia frente a la comodidad de las instalaciones en IMO - Instituto Médico Oncológico? Opciones: Muy Buena, Buena, Regular, Mala, Muy Mala",
                type = SatisfactionQuestionType.LIKERT_5,
                options = listOf("Muy Buena", "Buena", "Regular", "Mala", "Muy Mala"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P007",
                label = "Información sobre Derechos y Deberes",
                textTts = "¿Le informaron los Derechos y Deberes que usted tiene como usuario? Opciones: Sí, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P008",
                label = "Ejemplos de derechos",
                textTts = "Por favor, ¿Nos podría dar dos ejemplos de derechos y explicarlos con sus propias palabras? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = false
            ),
            SatisfactionQuestion(
                id = "P009",
                label = "Ejemplos de deberes",
                textTts = "¿Nos podría dar dos ejemplos de deberes y explicarlos con sus propias palabras? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = false
            ),
            SatisfactionQuestion(
                id = "P010",
                label = "Atención humanizada",
                textTts = "¿Considera usted que está siendo atendido en una institución humanizada? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P011",
                label = "Profesional se presentó por su nombre",
                textTts = "¿El profesional que lo atendió se presentó por su nombre? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P012",
                label = "Profesional lo llamó por su nombre",
                textTts = "¿El profesional que lo atendió lo llamó por su nombre? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P013",
                label = "Profesional habló claramente sobre la condición de salud",
                textTts = "¿El profesional le habló claramente acerca de su condición de salud? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P014",
                label = "Explicación de exámenes o procedimientos",
                textTts = "Si le ordenó exámenes o procedimientos, ¿Le fueron explicados? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P015",
                label = "Explicación de medicamentos",
                textTts = "Si le formuló medicamentos, ¿Le explicó para qué eran y cómo usarlos? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P016",
                label = "Información sobre atención médica",
                textTts = "¿Fue usted informado sobre aspectos relacionados con su atención? (toma de laboratorios, recomendaciones médicas). Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P017",
                label = "Información administrativa",
                textTts = "¿Fue usted informado sobre aspectos administrativos (autorizaciones, próximo control)? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P018",
                label = "Orden de evacuación en emergencia",
                textTts = "En caso de una emergencia en IMO, ¿Quién nos dará la orden de evacuación? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P019",
                label = "Ruta de evacuación",
                textTts = "¿Podría indicarnos en este momento la ruta de evacuación en caso de una emergencia? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P020",
                label = "Qué hacer al escuchar la alarma",
                textTts = "¿Qué es lo más importante en caso de escuchar la alarma sonora de emergencia? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P021",
                label = "Indicación sobre evacuación a punto seguro",
                textTts = "¿Le fue indicado que en caso de evacuación será llevado a un punto seguro hasta que cese la emergencia? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P022",
                label = "Uso del ascensor en emergencia",
                textTts = "En caso de emergencia, si usted estuviera en el segundo piso, ¿utilizaría el ascensor? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P023",
                label = "Disposición de residuos",
                textTts = "¿Le fue informado por parte del personal cómo se debe disponer los residuos en cada caneca según el color? ¿Podría explicarnos? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P024",
                label = "Importancia de segregar residuos",
                textTts = "¿Le explicaron la importancia de segregar correctamente los residuos para mantener un ambiente sano libre de contaminación? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P025",
                label = "Dónde manifestar quejas o sugerencias",
                textTts = "¿Sabe dónde manifestar una petición, queja, reclamo, sugerencia o felicitación? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P026",
                label = "Barrera de acceso al servicio",
                textTts = "¿Considera que hubo alguna barrera de acceso para recibir el servicio en IMO - Instituto Médico Oncológico? Opciones: Si, No, N/A",
                type = SatisfactionQuestionType.SINGLE_CHOICE_LIST,
                options = listOf("Si", "No", "N/A"),
                isRegulatoryKey = true
            ),
            SatisfactionQuestion(
                id = "P027",
                label = "Recomendación a familiares y amigos",
                textTts = "¿Recomendaría a sus familiares y amigos a IMO - Instituto Médico Oncológico? Opciones: Definitivamente sí, Probablemente sí, Probablemente no, Definitivamente no",
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