package com.esbot.temi.esbot_health.feature.satisfaction.domain.repository

import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionQuestion
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionMode

/**
 * Contrato para obtener configuración de la encuesta de satisfacción:
 * preguntas, modo por defecto, etc.
 * */

interface SatisfactionConfigRepository {

    /**
     * Devuelve la lista de preguntas por defecto.
     * */
    suspend fun getDefaultQuestions(): List<SatisfactionQuestion>

    /**
     * Devuelve el modo por defecto
     * */
    suspend fun getDefaultMode(): SatisfactionMode
}