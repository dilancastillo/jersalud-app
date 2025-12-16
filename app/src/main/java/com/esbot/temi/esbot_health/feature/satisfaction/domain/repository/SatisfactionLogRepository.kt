package com.esbot.temi.esbot_health.feature.satisfaction.domain.repository

import com.esbot.temi.esbot_health.feature.satisfaction.domain.model.SatisfactionSession

/**
 * Contrato para guardar sesiones de satisfacción.
 * El dominio solo guarda sesiones y esta interface define donde (Local, OneDrive, etc.)
 * */

interface SatisfactionLogRepository {

    /**
     * Guarda una sesión de satisfacción.
     * Puede ser local (CSV, Room) y/o remota (OneDrive, API).
     * */
    suspend fun saveSession(session: SatisfactionSession)
}