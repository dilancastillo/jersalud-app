package com.esbot.temi.esbot_health.feature.satisfaction.data.repository

import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionQuestion
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSurvey
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionMode
import com.esbot.temi.esbot_health.feature.satisfaction.domain.repository.SatisfactionConfigRepository

/**
 * Implementación sencilla que devuelve configuración "quemada":
 * preguntas desde SatisfactionSurvey y modo por defecto AUTO.
 * */

class DefaultSatisfactionConfigRepository : SatisfactionConfigRepository {

    override suspend fun getDefaultQuestions(): List<SatisfactionQuestion> =
        SatisfactionSurvey.shortSurvey()

    override suspend fun getDefaultMode(): SatisfactionMode =
        SatisfactionMode.AUTO
}