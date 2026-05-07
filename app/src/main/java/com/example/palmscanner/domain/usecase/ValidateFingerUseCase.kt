package com.example.palmscanner.domain.usecase

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.ValidationResult
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.repository.IValidationRepository
import javax.inject.Inject

/**
 * Thin wrapper around IValidationRepository.validateFinger().
 * Kept separate so it can be unit tested in isolation
 * and swapped for a real biometric SDK later.
 *
 * ⚠️ SIMULATED minutiae matching inside repository impl.
 */
class ValidateFingerUseCase @Inject constructor(
    private val validationRepository: IValidationRepository
) {
    suspend operator fun invoke(
        fingerBitmap: Bitmap,
        expectedHand: HandSide
    ): ValidationResult {
        return validationRepository.validateFinger(
            fingerBitmap = fingerBitmap,
            expectedHand = expectedHand
        )
    }
}