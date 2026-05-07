package com.example.palmscanner.domain.repository

import com.example.palmscanner.domain.model.CaptureMetadata
import kotlinx.coroutines.flow.Flow

/**
 * Contract for all CameraX operations.
 * Implementation lives in data/camera/CameraRepositoryImpl.kt
 */
interface ICameraRepository {

    /**
     * Returns current focus distance as a Flow.
     * Updated every time CameraX reports a new capture result.
     */
    val focusDistance: Flow<Float>

    /**
     * Triggers a still image capture.
     * @return Absolute file path of the saved image, or null on failure.
     */
    suspend fun captureImage(fileName: String): String?

    /**
     * Builds a CaptureMetadata snapshot at this exact moment.
     * Called right before saving an image so scores are accurate.
     */
    suspend fun buildCaptureMetadata(
        brightnessScore: Double,
        blurScore: Double,
        focusDistance: Float
    ): CaptureMetadata
}