package com.esbot.temi.esbot_health.core

/**
 * Sanea textos antes de escribirlos en CSV:
 * - Reemplaza saltos de línea por espacios
 * - Reemplaza comas por espacios para no romper el formato.
 * */

fun String.toCsvField(): String =
    replace("\n", " ").replace(",", " ")