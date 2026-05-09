package com.example.palmscanner.domain.usecase

import com.example.palmscanner.domain.model.enums.LightLevel
import javax.inject.Inject

class AnalyzeLightUseCase @Inject constructor() {

    data class LightAnalysis(
        val level: LightLevel,
        val score: Double,
        val message: String,
        val isAcceptable: Boolean  // false only for LOW light
    )

    operator fun invoke(lumaValue: Double): LightAnalysis {
        val level = LightLevel.fromLuma(lumaValue)

        val message = when (level) {
            LightLevel.LOW    -> "Too dark — move to a brighter area"
            LightLevel.NORMAL -> "Lighting is good"
            LightLevel.BRIGHT -> "Very bright — avoid direct sunlight"
        }

        return LightAnalysis(
            level = level,
            score = lumaValue,
            message = message,
            isAcceptable = level != LightLevel.LOW
        )
    }
}