package com.esbot.temi.esbot_health.education

enum class EducationTopic(
    val id: String,
    val displayName: String,
    val sequenceName: String
) {
    RIGHTS_DUTIES(
        id = "Derechos y deberes",
        displayName = "Derechos y deberes",
        sequenceName = "Derechos y deberes del usuario"
    ),
    EMERGENCY_ROUTES(
        id = "Rutas de emergencia",
        displayName = "Rutas de emergencia",
        sequenceName = "Rutas de emergencia"
    ),
    SANITARY_ROUTES(
        id = "Rutas sanitarias",
        displayName = "Rutas sanitarias",
        sequenceName = "Rutas sanitarias"
    ),
    HAND_HYGIENE(
        id = "Higiene de manos",
        displayName = "Higiene de manos",
        sequenceName = "Higiene de manos"
    ),
    ALARM_SIGNS(
        id = "Signos de alarma",
        displayName = "Signos de alarma",
        sequenceName = "Signos de alarma"
    ),
    PREOP(
        id = "Preparación preoperatoria",
        displayName = "Preparación preoperatoria",
        sequenceName = "Preparación preoperatoria"
    ),
    POSTOP(
        id = "Cuidados posoperatorios",
        displayName = "Cuidados posoperatorios",
        sequenceName = "Cuidados posoperatorios"
    ),
    cubiculo1(
    id = "POS - Quimioterapia",
    displayName = "IMO - Quimioterapia",
    sequenceName = "IMO - Quimioterapia"
    ),
    cubiculo2(
    id = "POS - Radioterapia",
    displayName = "IMO - Radioterapia",
    sequenceName = "IMO - Radioterapia"
    );

    //IMO - Quimioterapia


    companion object {
        fun allTopics(): List<EducationTopic> = values().toList()
    }
}