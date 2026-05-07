package com.example.palmscanner.data.ml

import com.example.palmscanner.domain.model.HandDetectionResult
import com.example.palmscanner.domain.repository.IHandDetectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HandDetectionRepositoryImpl @Inject constructor(
    private val handLandmarkAnalyzer: HandLandmarkAnalyzer
) : IHandDetectionRepository {

    override val detectionResults: Flow<HandDetectionResult> =
        handLandmarkAnalyzer.detectionResultFlow

    override fun startDetection() {
        handLandmarkAnalyzer.setup()
    }

    override fun stopDetection() {
        handLandmarkAnalyzer.teardown()
    }
}