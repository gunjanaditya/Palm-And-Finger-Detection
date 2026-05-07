package com.example.palmscanner.domain.usecase

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.CaptureMetadata
import com.example.palmscanner.domain.model.FingerCaptureResult
import com.example.palmscanner.domain.model.HandDetectionResult
import com.example.palmscanner.domain.model.enums.FingerName
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.model.enums.PalmSide
import com.example.palmscanner.domain.model.ValidationFailureReason
import com.example.palmscanner.domain.repository.ICameraRepository
import com.example.palmscanner.domain.repository.IStorageRepository
import com.example.palmscanner.domain.repository.IValidationRepository
import javax.inject.Inject

/**
 * Orchestrates single finger capture + validation:
 * 1. Validates detection state
 * 2. Validates finger against registered palm (simulated)
 * 3. Saves image to storage
 * 4. Returns FingerCaptureResult
 */
class CaptureFingerUseCase @Inject constructor(
    private val cameraRepository: ICameraRepository,
    private val storageRepository: IStorageRepository,
    private val validationRepository: IValidationRepository
) {
    sealed class Result {
        data class Success(val fingerResult: FingerCaptureResult) : Result()
        data class Failure(val reason: ValidationFailureReason) : Result()
    }

    suspend operator fun invoke(
        detectionResult: HandDetectionResult,
        fingerBitmap: Bitmap,
        targetFinger: FingerName,
        expectedHand: HandSide,
        currentBrightness: Double,
        currentBlur: Double,
        currentFocus: Float
    ): Result {

        // ── Guard: hand must be detected ────────────────────────────
        if (!detectionResult.isDetected) {
            return Result.Failure(ValidationFailureReason.NO_HAND_DETECTED)
        }

        // ── Guard: must show palm side (Requirement r) ──────────────
        if (detectionResult.palmSide == PalmSide.DORSAL) {
            return Result.Failure(ValidationFailureReason.DORSAL_SIDE)
        }

        // ── Guard: correct hand side ────────────────────────────────
        // Relaxed: MediaPipe often flickers between hand sides when
        // only a single finger is visible. We trust the palm registration
        // and only block if it is EXPLICITLY the opposite hand.
        if ((detectionResult.handSide == HandSide.LEFT && expectedHand == HandSide.RIGHT) ||
            (detectionResult.handSide == HandSide.RIGHT && expectedHand == HandSide.LEFT)) {
            return Result.Failure(ValidationFailureReason.WRONG_HAND)
        }

        // ── Guard: blur check ───────────────────────────────────────
        if (detectionResult.isBlurry) {
            return Result.Failure(ValidationFailureReason.BLUR_TOO_HIGH)
        }

        // ── Validate finger against palm (⚠️ SIMULATED) ────────────
        val validation = validationRepository.validateFinger(
            fingerBitmap = fingerBitmap,
            expectedHand = expectedHand
        )

        // ── Build file name ─────────────────────────────────────────
        val fileName = storageRepository.buildFingerFileName(
            handSide = expectedHand.name,
            fingerName = targetFinger.name
        )

        // ── Save image regardless of match result ───────────────────
        val savedPath = storageRepository.saveBitmap(fingerBitmap, fileName)
            ?: return Result.Failure(ValidationFailureReason.NO_HAND_DETECTED)

        // ── Build metadata ──────────────────────────────────────────
        val metadata = cameraRepository.buildCaptureMetadata(
            brightnessScore = currentBrightness,
            blurScore = currentBlur,
            focusDistance = currentFocus
        )

        // ✅ Requirement (s): Save metadata to log
        storageRepository.saveMetadata(metadata)

        return Result.Success(
            fingerResult = FingerCaptureResult(
                fingerName = targetFinger,
                imagePath = savedPath,
                isValid = validation.isMatch,
                matchScore = validation.score,
                metadata = metadata,
                errorMessage = validation.failureReason?.message
            )
        )
    }
}