package com.example.palmscanner.domain.repository

import com.example.palmscanner.domain.model.CaptureMetadata
import kotlinx.coroutines.flow.Flow


interface ICameraRepository {
    val focusDistance: Flow<Float>
    suspend fun captureImage(fileName: String): String?
    suspend fun buildCaptureMetadata(
        brightnessScore: Double,
        blurScore: Double,
        focusDistance: Float
    ): CaptureMetadata
}