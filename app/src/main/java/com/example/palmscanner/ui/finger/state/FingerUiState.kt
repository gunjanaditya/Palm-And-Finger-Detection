package com.example.palmscanner.ui.finger.state

import com.example.palmscanner.domain.model.FingerCaptureResult
import com.example.palmscanner.domain.model.enums.FingerName

sealed class FingerUiState {
    object Idle : FingerUiState()
    data class WaitingForFinger(val fingerName: FingerName, val index: Int) : FingerUiState()
    data class FingerDetected(val fingerName: FingerName) : FingerUiState()
    object ReadyToCapture : FingerUiState()
    data class ValidationFailed(val reason: String) : FingerUiState()
    data class FingerCaptured(val result: FingerCaptureResult) : FingerUiState()
    object AllFingersComplete : FingerUiState()
}