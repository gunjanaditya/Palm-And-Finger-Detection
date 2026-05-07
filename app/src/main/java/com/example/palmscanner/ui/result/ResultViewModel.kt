package com.example.palmscanner.ui.result

import androidx.lifecycle.ViewModel
import com.example.palmscanner.domain.model.ScanSession
import com.example.palmscanner.ui.result.state.ResultUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadSession(session: ScanSession) {
        _uiState.value = ResultUiState.Success(session)
    }
}