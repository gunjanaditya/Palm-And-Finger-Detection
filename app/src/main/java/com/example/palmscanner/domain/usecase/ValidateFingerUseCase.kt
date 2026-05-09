package com.example.palmscanner.domain.usecase

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.ValidationResult
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.repository.IValidationRepository
import javax.inject.Inject

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