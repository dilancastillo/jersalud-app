package com.esbot.temi.esbot_health.feature.satisfaction.domain.model

import com.esbot.temi.esbot_health.education.BedInfo

/**
 * Sesión completa de una encuesta de satisfacción en una cama concreta.
 * */

data class SatisfactionSession(
    val timestampMillis: Long,
    val mode: String,
    val bed: BedInfo,
    val answers: List<SatisfactionAnswer>
)