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
        BedInfo("MI_301A", "Hab 301 – Cama A", "hab301a"),
        BedInfo("MI_302A", "Hab 302 – Cama A", "hab302a"),
        BedInfo("MI_302B", "Hab 302 – Cama B", "hab302b"),
        BedInfo("MI_303A", "Hab 303 – Cama A", "hab303a")
    )
}