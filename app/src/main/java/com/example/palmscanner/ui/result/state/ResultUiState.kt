package com.example.palmscanner.ui.result.state

import com.example.palmscanner.domain.model.ScanSession

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(val session: ScanSession) : ResultUiState()
    data class Error(val message: String) : ResultUiState()
}