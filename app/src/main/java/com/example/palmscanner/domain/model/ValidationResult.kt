package com.example.palmscanner.domain.model

/**
 * Result of the simulated minutiae validation.
 *
 * ⚠️ SIMULATED — In production this would use a real biometric SDK.
 *
 * @param isMatch       Whether finger matches the captured palm
 * @param score         Confidence score (0.0–1.0)
 * @param failureReason Set when isMatch == false
 */
data class ValidationResult(
    val isMatch: Boolean,
    val score: Float,
    val failureReason: ValidationFailureReason? = null
) {
    companion object {
        /** Minimum score to consider a valid match */
        const val MATCH_THRESHOLD = 0.65f
    }
}

/**
 * Reason codes for validation failure.
 * Each maps to a specific error message shown in the UI.
 */
enum class ValidationFailureReason(val message: String) {
    DIFFERENT_PERSON("Finger does not match. Please use the same hand."),
    WRONG_HAND("Incorrect finger. Please use the correct hand side."),
    DORSAL_SIDE("Please show the palm side of your hand, not the back."),
    BLUR_TOO_HIGH("Image is too blurry. Please hold your hand steady."),
    LOW_LIGHT("Insufficient lighting. Please move to a brighter area."),
    NO_HAND_DETECTED("No hand detected. Please position your hand in frame.")
}