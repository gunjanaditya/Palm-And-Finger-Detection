package com.example.palmscanner.domain.repository

import com.example.palmscanner.domain.model.HandDetectionResult
import kotlinx.coroutines.flow.Flow

interface IHandDetectionRepository {

    val detectionResults: Flow<HandDetectionResult>
    fun startDetection()
    fun stopDetection()
}