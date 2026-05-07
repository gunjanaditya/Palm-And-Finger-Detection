package com.example.palmscanner.domain.usecase

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.CaptureMetadata
import com.example.palmscanner.domain.model.HandDetectionResult
import com.example.palmscanner.domain.model.ValidationResult
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.model.enums.PalmSide
import com.example.palmscanner.domain.model.ValidationFailureReason
import com.example.palmscanner.domain.repository.ICameraRepository
import com.example.palmscanner.domain.repository.IStorageRepository
import com.example.palmscanner.domain.repository.IValidationRepository
import javax.inject.Inject

/**
 * Orchestrates the full palm capture flow:
 * 1. Validates detection state (palm side, blur, light)
 * 2. Triggers image capture
 * 3. Saves to storage
 * 4. Registers palm as validation reference
 *
 * Returns a sealed Result — ViewModel never handles raw exceptions.
 */
class CapturePalmUseCase @Inject constructor(
    private val cameraRepository: ICameraRepository,
    private val storageRepository: IStorageRepository,
    private val validationRepository: IValidationRepository
) {
    sealed class Result {
        data class Success(
            val imagePath: String,
            val metadata: CaptureMetadata
        ) : Result()

        data class Failure(val reason: ValidationFailureReason) : Result()
    }

    suspend operator fun invoke(
        detectionResult: HandDetectionResult,
        palmBitmap: Bitmap,
        currentBrightness: Double,
        currentBlur: Double,
        currentFocus: Float
    ): Result {

        // ── Guard: must show palm side ──────────────────────────────
        if (detectionResult.palmSide == PalmSide.DORSAL) {
            return Result.Failure(ValidationFailureReason.DORSAL_SIDE)
        }

        // ── Guard: light level check ────────────────────────────────
        if (detectionResult.isBlurry) {
            return Result.Failure(ValidationFailureReason.BLUR_TOO_HIGH)
        }

        // ── Guard: hand must be detected ────────────────────────────
        if (!detectionResult.isDetected) {
            return Result.Failure(ValidationFailureReason.NO_HAND_DETECTED)
        }

        // ── Build file name ─────────────────────────────────────────
        val fileName = storageRepository.buildPalmFileName(
            detectionResult.handSide.name
        )

        // ── Save image ──────────────────────────────────────────────
        val savedPath = storageRepository.saveBitmap(palmBitmap, fileName)
            ?: return Result.Failure(ValidationFailureReason.NO_HAND_DETECTED)

        // ── Build metadata snapshot ─────────────────────────────────
        val metadata = cameraRepository.buildCaptureMetadata(
            brightnessScore = currentBrightness,
            blurScore = currentBlur,
            focusDistance = currentFocus
        )

        // ✅ Requirement (s): Save metadata to log
        storageRepository.saveMetadata(metadata)

        // ── Register palm for validation ────────────────────────────
        validationRepository.registerPalm(palmBitmap, detectionResult.handSide)

        return Result.Success(
            imagePath = savedPath,
            metadata = metadata
        )
    }
}