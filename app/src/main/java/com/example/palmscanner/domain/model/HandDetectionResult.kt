package com.example.palmscanner.domain.model

import com.example.palmscanner.domain.model.enums.FingerName
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.model.enums.LightLevel
import com.example.palmscanner.domain.model.enums.PalmSide

/**
 * Result of a single frame analysis by MediaPipe Hands.
 * Produced by HandLandmarkAnalyzer, consumed by ViewModels.
 *
 * @param isDetected        Whether a hand is visible in frame
 * @param handSide          LEFT / RIGHT / UNKNOWN
 * @param palmSide          PALM / DORSAL / UNKNOWN
 * @param fingersExtended   Boolean list [thumb, index, middle, ring, little]
 * @param fingerCount       Number of extended fingers (0–5)
 * @param lightLevel        Current frame luminosity level
 * @param brightnessScore   Raw luma value (0.0–255.0)
 * @param blurScore         Laplacian variance — higher = sharper
 * @param isBlurry          true if blurScore < BLUR_THRESHOLD
 * @param focusDistance     Estimated focus distance in meters (from CameraX)
 * @param detectedFingerName Finger currently extended (for finger capture screen)
 */
data class HandDetectionResult(
    val isDetected: Boolean = false,
    val handSide: HandSide = HandSide.UNKNOWN,
    val palmSide: PalmSide = PalmSide.UNKNOWN,
    val fingersExtended: List<Boolean> = List(5) { false },
    val fingerCount: Int = 0,
    val lightLevel: LightLevel = LightLevel.NORMAL,
    val brightnessScore: Double = 0.0,
    val blurScore: Double = 0.0,
    val isBlurry: Boolean = false,
    val focusDistance: Float = 0f,
    val detectedFingerName: FingerName? = null
) {
    companion object {
        /** Below this Laplacian variance, image is considered blurry */
        const val BLUR_THRESHOLD = 100.0

        /** Returns an empty/default result for initial state */
        fun empty() = HandDetectionResult()
    }
}