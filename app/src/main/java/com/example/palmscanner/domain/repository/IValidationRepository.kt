package com.example.palmscanner.domain.repository

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.ValidationResult
import com.example.palmscanner.domain.model.enums.HandSide

/**
 * Contract for finger-palm validation logic.
 *
 * ⚠️ SIMULATED — Implementation uses hashed feature vectors,
 * not real biometric minutiae extraction.
 *
 * Implementation lives in data/validation/ValidationRepositoryImpl.kt
 */
interface IValidationRepository {

    /**
     * Registers a palm image as the reference for this session.
     * Must be called once after palm capture, before any finger validation.
     * @param palmBitmap  The captured palm image
     * @param handSide    Which hand was captured
     */
    suspend fun registerPalm(palmBitmap: Bitmap, handSide: HandSide)

    /**
     * Validates a finger image against the registered palm.
     * @param fingerBitmap  The captured finger image
     * @param expectedHand  The hand side expected for this finger
     * @return ValidationResult with score and failure reason if any
     */
    suspend fun validateFinger(
        fingerBitmap: Bitmap,
        expectedHand: HandSide
    ): ValidationResult

    /**
     * Clears registered palm data.
     * Call when starting a new session.
     */
    fun clearSession()
}