package com.example.palmscanner.ui.permission

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {

    private val _allGranted = MutableStateFlow(false)
    val allGranted: StateFlow<Boolean> = _allGranted.asStateFlow()

    fun onPermissionsResult(granted: Boolean) {
        _allGranted.value = granted
    }
}