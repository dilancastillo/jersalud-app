package com.esbot.temi.esbot_health.education

enum class EducationTopic(
    val id: String,
    val displayName: String,
    val sequenceName: String
) {
    RIGHTS_DUTIES(
        id = "rights_duties",
        displayName = "Derechos y deberes",
        sequenceName = "Derechos y deberes del usuario"
    ),
    EMERGENCY_ROUTES(
        id = "emergency_routes",
        displayName = "Rutas de emergencia",
        sequenceName = "Rutas de emergencia"
    ),
    SANITARY_ROUTES(
        id = "sanitary_routes",
        displayName = "Rutas sanitarias",
        sequenceName = "Rutas sanitarias"
    ),
    HAND_HYGIENE(
        id = "hand_hygiene",
        displayName = "Higiene de manos",
        sequenceName = "Higiene de manos"
    ),
    ALARM_SIGNS(
        id = "alarm_signs",
        displayName = "Signos de alarma",
        sequenceName = "Signos de alarma"
    ),
    PREOP(
        id = "preop",
        displayName = "Preparación preoperatoria",
        sequenceName = "Preparación preoperatoria"
    ),
    POSTOP(
        id = "postop",
        displayName = "Cuidados posoperatorios",
        sequenceName = "Cuidados posoperatorios"
    ),
    cubiculo1(
    id = "postop",
    displayName = "IMO - Quimioterapia",
    sequenceName = "IMO - Quimioterapia"
    ),
    cubiculo2(
    id = "postop",
    displayName = "IMO - Radioterapia",
    sequenceName = "IMO - Radioterapia"
    );

    //IMO - Quimioterapia


    companion object {
        fun allTopics(): List<EducationTopic> = values().toList()
    }
}