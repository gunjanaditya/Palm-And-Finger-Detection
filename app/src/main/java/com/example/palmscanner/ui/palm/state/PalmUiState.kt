package com.example.palmscanner.ui.palm.state

import com.example.palmscanner.domain.model.CaptureMetadata
import com.example.palmscanner.domain.model.HandDetectionResult

sealed class PalmUiState {
    object Idle : PalmUiState()
    data class Detecting(val result: HandDetectionResult) : PalmUiState()
    object ReadyToCapture : PalmUiState()
    data class Captured(val imagePath: String, val metadata: CaptureMetadata) : PalmUiState()
    data class Error(val message: String) : PalmUiState()
}