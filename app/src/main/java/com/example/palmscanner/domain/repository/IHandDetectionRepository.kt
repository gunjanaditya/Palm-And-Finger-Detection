package com.example.palmscanner.domain.repository

import com.example.palmscanner.domain.model.HandDetectionResult
import kotlinx.coroutines.flow.Flow

/**
 * Contract for MediaPipe hand detection pipeline.
 * Implementation lives in data/ml/HandDetectionRepositoryImpl.kt
 */
interface IHandDetectionRepository {

    /**
     * Continuous stream of detection results — one per analyzed frame.
     * ViewModel collects this Flow and maps it to UiState.
     */
    val detectionResults: Flow<HandDetectionResult>

    /**
     * Starts the MediaPipe inference engine.
     * Must be called before detectionResults emits anything.
     */
    fun startDetection()

    /**
     * Stops inference and releases MediaPipe resources.
     * Call in ViewModel's onCleared() or Fragment's onDestroyView().
     */
    fun stopDetection()
}