package com.example.palmscanner.domain.model

import android.os.Parcelable
import com.example.palmscanner.domain.model.enums.HandSide
import kotlinx.parcelize.Parcelize

/**
 * Represents a complete scan session — palm + all 5 fingers.
 * Passed from FingerDetectionFragment → ResultFragment via Safe Args.
 *
 * @param palmImagePath     Absolute path to palm image
 * @param handSide          Which hand was scanned
 * @param fingerResults     List of 5 FingerCaptureResult (in capture order)
 * @param palmMetadata      Metadata from palm capture moment
 */
@Parcelize
data class ScanSession(
    val palmImagePath: String,
    val handSide: HandSide,
    val fingerResults: List<FingerCaptureResult>,
    val palmMetadata: CaptureMetadata
) : Parcelable {

    /** true only if ALL 5 fingers passed validation */
    val isFullyValid: Boolean
        get() = fingerResults.size == 5 && fingerResults.all { it.isValid }

    /** Number of fingers that passed validation */
    val validFingerCount: Int
        get() = fingerResults.count { it.isValid }

    /** Average match score across all fingers */
    val averageMatchScore: Float
        get() = if (fingerResults.isEmpty()) 0f
        else fingerResults.map { it.matchScore }.average().toFloat()
}