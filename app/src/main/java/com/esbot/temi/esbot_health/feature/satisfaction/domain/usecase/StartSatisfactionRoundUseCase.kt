package com.esbot.temi.esbot_health.feature.satisfaction.domain.usecase

import com.esbot.temi.esbot_health.education.BedInfo
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionMode
import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionRoundConfig
import com.esbot.temi.esbot_health.feature.satisfaction.domain.repository.SatisfactionConfigRepository

/**
 * Caso de uso para iniciar una ronda de satisfacción:
 * - Obtiene las preguntas por defecto desde el repositorio de config.
 * - Decide el modo final (el que viene o el modo por defecto).
 * - Devuelve una configuración de ronda lista para usar en el ViewModel.
 * */

class StartSatisfactionRoundUseCase(
    private val configRepository: SatisfactionConfigRepository
) {

    /**
     * @param mode Opcional: si es null usará el modo por defecto del sistema.
     * @param beds Camas seleccionadas por el usuario en la configuración.
     * */
    suspend operator fun invoke(
        mode: SatisfactionMode?,
        beds: List<BedInfo>
    ): SatisfactionRoundConfig {
        val questions = configRepository.getDefaultQuestions()
        val defaultMode = configRepository.getDefaultMode()
        val finalMode = mode ?: defaultMode

        return SatisfactionRoundConfig(
            mode = finalMode,
            beds = beds,
            questions = questions
        )
    }
}