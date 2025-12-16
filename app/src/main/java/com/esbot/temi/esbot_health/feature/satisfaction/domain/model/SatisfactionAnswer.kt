package com.esbot.temi.esbot_health.feature.satisfaction.domain.model

/**
 * Respuesta a una pregunta de satisfacción.
 * */

data class SatisfactionAnswer(
    val questionId: String,
    val questionLabel: String,
    val optionIndex: Int,
    val optionText: String,
    val isRegulatoryKey: Boolean
)