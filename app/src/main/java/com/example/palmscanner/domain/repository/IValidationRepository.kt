package com.example.palmscanner.domain.repository

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.ValidationResult
import com.example.palmscanner.domain.model.enums.HandSide

interface IValidationRepository {
    suspend fun registerPalm(palmBitmap: Bitmap, handSide: HandSide)
    suspend fun validateFinger(
        fingerBitmap: Bitmap,
        expectedHand: HandSide
    ): ValidationResult
    fun clearSession()
}