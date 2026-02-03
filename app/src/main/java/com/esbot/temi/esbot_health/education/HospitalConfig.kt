package com.esbot.temi.esbot_health.education

data class BedInfo(
    val id: String,         // ej: "MI_301A"
    val label: String,      // ej: "Hab 301 – Cama A"
    val locationName: String // nombre EXACTO de la locación en el mapa Temi
)

object HospitalConfig {

    const val FLOOR_NAME: String = "Piso 3 – Medicina Interna"
    const val NURSING_LOCATION: String = "enfermeria"

    val bedsMi: List<BedInfo> = listOf(
        BedInfo("cubiculo1", "Cubiculo 1", "cubiculo1"),
        BedInfo("cubiculo2", "Cubiculo 2", "cubiculo2")
    )
}