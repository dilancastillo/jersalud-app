package com.esbot.temi.esbot_health.feature.satisfaction.domain.model

import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionQuestion

/**
 * Configuración de una ronda de satisfacción:
 * modo, camas seleccionadas y preguntas que se van a usar.
 * */

data class SatisfactionRoundConfig(
    val mode: SatisfactionMode,
    val beds: List<BedInfo>,
    val questions: List<SatisfactionQuestion>
)