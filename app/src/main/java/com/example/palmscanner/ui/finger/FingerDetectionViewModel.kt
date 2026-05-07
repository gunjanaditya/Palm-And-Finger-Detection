package com.example.palmscanner.ui.finger

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.palmscanner.domain.model.FingerCaptureResult
import com.example.palmscanner.domain.model.HandDetectionResult
import com.example.palmscanner.domain.model.ScanSession
import com.example.palmscanner.domain.model.enums.FingerName
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.repository.IHandDetectionRepository
import com.example.palmscanner.domain.usecase.CaptureFingerUseCase
import com.example.palmscanner.ui.finger.state.FingerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FingerDetectionViewModel @Inject constructor(
    private val handDetectionRepository: IHandDetectionRepository,
    private val captureFingerUseCase: CaptureFingerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FingerUiState>(FingerUiState.Idle)
    val uiState: StateFlow<FingerUiState> = _uiState.asStateFlow()

    private val _detectionResult = MutableStateFlow(HandDetectionResult.empty())
    val detectionResult: StateFlow<HandDetectionResult> = _detectionResult.asStateFlow()

    private val capturedFingers = mutableListOf<FingerCaptureResult>()
    private val fingerOrder     = FingerName.captureOrder()
    private var currentIndex    = 0

    var palmImagePath = ""
    var expectedHand  = HandSide.UNKNOWN

    init {
        observeDetection()
    }

    private fun observeDetection() {
        viewModelScope.launch {
            handDetectionRepository.detectionResults.collect { result ->
                _detectionResult.value = result
                val currentFinger = fingerOrder.getOrNull(currentIndex) ?: return@collect
                _uiState.value = when {
                    result.isDetected && !result.isBlurry ->
                        FingerUiState.ReadyToCapture
                    result.isDetected ->
                        FingerUiState.FingerDetected(currentFinger)
                    else ->
                        FingerUiState.WaitingForFinger(currentFinger, currentIndex)
                }
            }
        }
    }

    fun captureFinger(bitmap: Bitmap) {
        val current       = _detectionResult.value
        val targetFinger  = fingerOrder.getOrNull(currentIndex) ?: return

        viewModelScope.launch {
            val result = captureFingerUseCase(
                detectionResult   = current,
                fingerBitmap      = bitmap,
                targetFinger      = targetFinger,
                expectedHand      = expectedHand,
                currentBrightness = current.brightnessScore,
                currentBlur       = current.blurScore,
                currentFocus      = current.focusDistance
            )
            when (result) {
                is CaptureFingerUseCase.Result.Success -> {
                    capturedFingers.add(result.fingerResult)
                    currentIndex++
                    if (currentIndex >= fingerOrder.size) {
                        _uiState.value = FingerUiState.AllFingersComplete
                    } else {
                        _uiState.value = FingerUiState.FingerCaptured(result.fingerResult)
                    }
                }
                is CaptureFingerUseCase.Result.Failure ->
                    _uiState.value = FingerUiState.ValidationFailed(result.reason.message)
            }
        }
    }

    fun buildScanSession(palmMetadata: com.example.palmscanner.domain.model.CaptureMetadata) =
        ScanSession(
            palmImagePath  = palmImagePath,
            handSide       = expectedHand,
            fingerResults  = capturedFingers.toList(),
            palmMetadata   = palmMetadata
        )

    fun startDetection() = handDetectionRepository.startDetection()
    fun stopDetection()  = handDetectionRepository.stopDetection()
    fun getCurrentFinger() = fingerOrder.getOrNull(currentIndex)
    fun getCurrentIndex()  = currentIndex
}