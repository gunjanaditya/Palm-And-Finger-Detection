package com.example.palmscanner.ui.palm

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.palmscanner.domain.model.HandDetectionResult
import com.example.palmscanner.domain.model.ValidationFailureReason
import com.example.palmscanner.domain.model.enums.PalmSide
import com.example.palmscanner.domain.repository.IHandDetectionRepository
import com.example.palmscanner.domain.usecase.CapturePalmUseCase
import com.example.palmscanner.ui.palm.state.PalmUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PalmDetectionViewModel @Inject constructor(
    private val handDetectionRepository: IHandDetectionRepository,
    private val capturePalmUseCase: CapturePalmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PalmUiState>(PalmUiState.Idle)
    val uiState: StateFlow<PalmUiState> = _uiState.asStateFlow()

    private val _detectionResult = MutableStateFlow(HandDetectionResult.empty())
    val detectionResult: StateFlow<HandDetectionResult> = _detectionResult.asStateFlow()

    init {
        observeDetection()
    }

    private fun observeDetection() {
        viewModelScope.launch {
            handDetectionRepository.detectionResults.collect { result ->
                _detectionResult.value = result
                if (_uiState.value !is PalmUiState.Captured) {
                    _uiState.value = if (result.isDetected &&
                        result.palmSide == PalmSide.PALM &&
                        !result.isBlurry
                    ) PalmUiState.ReadyToCapture
                    else PalmUiState.Detecting(result)
                }
            }
        }
    }

    fun capturePalm(bitmap: Bitmap) {
        val current = _detectionResult.value
        viewModelScope.launch {
            val result = capturePalmUseCase(
                detectionResult   = current,
                palmBitmap        = bitmap,
                currentBrightness = current.brightnessScore,
                currentBlur       = current.blurScore,
                currentFocus      = current.focusDistance
            )
            when (result) {
                is CapturePalmUseCase.Result.Success ->
                    _uiState.value = PalmUiState.Captured(result.imagePath, result.metadata)
                is CapturePalmUseCase.Result.Failure ->
                    _uiState.value = PalmUiState.Error(result.reason.message)
            }
        }
    }

    fun startDetection() = handDetectionRepository.startDetection()
    fun stopDetection()  = handDetectionRepository.stopDetection()
}