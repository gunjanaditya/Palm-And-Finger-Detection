package com.example.palmscanner.data.ml

import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.model.enums.PalmSide
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

class PalmSideDetector @Inject constructor() {

    companion object {
        private const val WRIST      = 0
        private const val INDEX_MCP  = 5
        private const val LITTLE_MCP = 17

        private const val CONFIDENCE_THRESHOLD = 0.01f
    }

    fun detect(landmarks: List<NormalizedLandmark>, handSide: HandSide = HandSide.UNKNOWN): PalmSide {
        if (landmarks.size < 21) return PalmSide.UNKNOWN

        val wrist     = landmarks[WRIST]
        val indexMcp  = landmarks[INDEX_MCP]
        val littleMcp = landmarks[LITTLE_MCP]

        // Vector A: Wrist → Index MCP
        val ax = indexMcp.x()  - wrist.x()
        val ay = indexMcp.y()  - wrist.y()

        // Vector B: Wrist → Little MCP
        val bx = littleMcp.x() - wrist.x()
        val by = littleMcp.y() - wrist.y()

        // Cross product Z component
        val crossZ = (ax * by) - (ay * bx)

        if (Math.abs(crossZ) < CONFIDENCE_THRESHOLD) return PalmSide.UNKNOWN

        // ── Right hand cross product is opposite to left hand ──
        // For LEFT hand:  crossZ > 0 = PALM
        // For RIGHT hand: crossZ > 0 = DORSAL (inverted)
        return when (handSide) {
            HandSide.RIGHT -> if (crossZ > 0) PalmSide.DORSAL else PalmSide.PALM
            else           -> if (crossZ > 0) PalmSide.PALM   else PalmSide.DORSAL
        }
    }

    fun getConfidence(landmarks: List<NormalizedLandmark>): Float {
        if (landmarks.size < 21) return 0f

        val wrist     = landmarks[WRIST]
        val indexMcp  = landmarks[INDEX_MCP]
        val littleMcp = landmarks[LITTLE_MCP]

        val ax = indexMcp.x()  - wrist.x()
        val ay = indexMcp.y()  - wrist.y()
        val bx = littleMcp.x() - wrist.x()
        val by = littleMcp.y() - wrist.y()

        val crossZ    = abs((ax * by) - (ay * bx))

        // Normalize by the product of vector magnitudes
        val magA = sqrt(ax * ax + ay * ay)
        val magB = sqrt(bx * bx + by * by)
        val denom = magA * magB

        return if (denom == 0f) 0f
        else (crossZ / denom).coerceIn(0f, 1f)
    }

    fun getMessage(palmSide: PalmSide): String = when (palmSide) {
        PalmSide.PALM    -> "Palm side detected ✅"
        PalmSide.DORSAL  -> "Please show the palm side of your hand ⚠️"
        PalmSide.UNKNOWN -> "Position your hand flat in the frame"
    }
}