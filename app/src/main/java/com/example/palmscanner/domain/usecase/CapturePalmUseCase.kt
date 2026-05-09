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

        if (detectionResult.palmSide == PalmSide.DORSAL) {
            return Result.Failure(ValidationFailureReason.DORSAL_SIDE)
        }

        if (detectionResult.isBlurry) {
            return Result.Failure(ValidationFailureReason.BLUR_TOO_HIGH)
        }

        if (!detectionResult.isDetected) {
            return Result.Failure(ValidationFailureReason.NO_HAND_DETECTED)
        }

        val fileName = storageRepository.buildPalmFileName(
            detectionResult.handSide.name
        )

        val savedPath = storageRepository.saveBitmap(palmBitmap, fileName)
            ?: return Result.Failure(ValidationFailureReason.NO_HAND_DETECTED)

        val metadata = cameraRepository.buildCaptureMetadata(
            brightnessScore = currentBrightness,
            blurScore = currentBlur,
            focusDistance = currentFocus
        )

        // ✅ Requirement (s): Save metadata to log
        storageRepository.saveMetadata(metadata)

        validationRepository.registerPalm(palmBitmap, detectionResult.handSide)

        return Result.Success(
            imagePath = savedPath,
            metadata = metadata
        )
    }
}