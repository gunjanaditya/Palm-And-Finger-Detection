package com.example.palmscanner.data.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.palmscanner.domain.model.enums.LightLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LuminosityAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {

    companion object {
        private const val EMIT_INTERVAL_MS = 200L
    }

    private val _brightnessFlow = MutableStateFlow(0.0)
    val brightnessFlow: StateFlow<Double> = _brightnessFlow.asStateFlow()

    private val _lightLevelFlow = MutableStateFlow(LightLevel.NORMAL)
    val lightLevelFlow: StateFlow<LightLevel> = _lightLevelFlow.asStateFlow()

    private var lastEmitTime = 0L

    override fun analyze(image: ImageProxy) {
        try {
            val currentTime = System.currentTimeMillis()

            // Throttle: skip frames if not enough time has passed
            if (currentTime - lastEmitTime < EMIT_INTERVAL_MS) {
                image.close()
                return
            }

            lastEmitTime = currentTime

            // ── Extract luma from Y plane ───────────────────────────
            val luma = calculateLuma(image)

            // ── Emit new values ─────────────────────────────────────
            _brightnessFlow.value = luma
            _lightLevelFlow.value = LightLevel.fromLuma(luma)

        } finally {
            // CRITICAL: Always close ImageProxy to avoid camera stalling
            image.close()
        }
    }

    private fun calculateLuma(image: ImageProxy): Double {
        val yPlane = image.planes[0]
        val yBuffer = yPlane.buffer
        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride

        val width = image.width
        val height = image.height

        // Rewind buffer to start
        yBuffer.rewind()

        var sum = 0L
        var count = 0

        // Sample every 4th row and every 4th column for performance
        val rowStep = maxOf(1, yRowStride * 4)
        val colStep = maxOf(1, yPixelStride * 4)

        var rowOffset = 0
        while (rowOffset < yBuffer.limit() && (rowOffset / yRowStride) < height) {
            var colOffset = rowOffset
            while (colOffset < rowOffset + width * yPixelStride &&
                colOffset < yBuffer.limit()) {
                sum += (yBuffer[colOffset].toInt() and 0xFF)
                count++
                colOffset += colStep
            }
            rowOffset += rowStep
        }

        return if (count == 0) 0.0 else sum.toDouble() / count
    }

    fun getCurrentBrightness(): Double = _brightnessFlow.value
}