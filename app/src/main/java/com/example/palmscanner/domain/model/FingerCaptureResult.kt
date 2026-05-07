package com.example.palmscanner.domain.model

import android.os.Parcelable
import com.example.palmscanner.domain.model.enums.FingerName
import kotlinx.parcelize.Parcelize

/**
 * Result of capturing a single finger image.
 * Holds the saved image path, validation result, and metadata.
 * Parcelable for Safe Args passing to Result screen.
 *
 * @param fingerName    Which finger this capture belongs to
 * @param imagePath     Absolute path to saved image in Finger Data/ folder
 * @param isValid       Whether finger matched palm (simulated)
 * @param matchScore    Simulated minutiae match score (0.0–1.0)
 * @param metadata      Camera/device metadata at time of capture
 * @param errorMessage  Set when isValid == false
 */
@Parcelize
data class FingerCaptureResult(
    val fingerName: FingerName,
    val imagePath: String,
    val isValid: Boolean,
    val matchScore: Float = 0f,
    val metadata: CaptureMetadata,
    val errorMessage: String? = null
) : Parcelable