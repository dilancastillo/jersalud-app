package com.esbot.temi.esbot_health.feature.satisfaction.domain.usecase

import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionAnswer
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionMode
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSession
import com.esbot.temi.esbot_health.feature.satisfaction.domain.repository.SatisfactionLogRepository

/**
 * Caso de uso para finalizar una ronda en una cama
 * y persistir la sesión de satisfacción.
 * */

class FinishSatisfactionRoundUseCase(
    private val logRepository: SatisfactionLogRepository
) {
    /**
     * Construye la sesión y la guarda a través del repositorio.
     *
     * @param mode Modo en que se realizó la ronda (AUTO/INDIVIDUAL).
     * @param bed Cama donde se aplicó la encuesta.
     * @param answers Respuestas dadas por el paciente en esta cama.
     * @param timestampMillis Momento en que se finaliza la sesión.
     * */

    suspend operator fun invoke(
        mode: SatisfactionMode,
        bed: BedInfo,
        answers: List<SatisfactionAnswer>,
        timestampMillis: Long = System.currentTimeMillis()
    ) {
        val session = SatisfactionSession(
            timestampMillis = timestampMillis,
            mode = mode.name,
            bed = bed,
            answers = answers
        )

        logRepository.saveSession(session)
    }
}