package com.example.palmscanner.domain.model

import android.os.Parcelable
import com.example.palmscanner.domain.model.enums.CameraType
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaptureMetadata(
    val brightnessScore: Double = 0.0,
    val blurScore: Double = 0.0,
    val focusDistance: Float = 0f,
    val cameraType: CameraType = CameraType.BACK,
    val deviceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    fun brightnessDisplay(): String = "%.2f".format(brightnessScore)

    fun blurDisplay(): String = "%.1f".format(blurScore)

    fun focusDisplay(): String = "%.2fm".format(focusDistance)
}