package com.example.palmscanner.domain.model.enums

enum class FingerName(val displayName: String) {
    THUMB("Thumb"),
    INDEX("Index"),
    MIDDLE("Middle"),
    RING("Ring"),
    LITTLE("Little");

    companion object {
        fun captureOrder(): List<FingerName> = values().toList()
    }
}