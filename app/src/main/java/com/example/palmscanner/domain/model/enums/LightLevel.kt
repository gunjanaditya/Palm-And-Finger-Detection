package com.example.palmscanner.domain.model.enums

enum class LightLevel(val displayLabel: String) {
    LOW("Low Light ⚠️"),
    NORMAL("Normal 💡"),
    BRIGHT("Bright ☀️");

    companion object {
        fun fromLuma(luma: Double): LightLevel = when {
            luma < 80.0  -> LOW
            luma > 200.0 -> BRIGHT
            else         -> NORMAL
        }
    }
}