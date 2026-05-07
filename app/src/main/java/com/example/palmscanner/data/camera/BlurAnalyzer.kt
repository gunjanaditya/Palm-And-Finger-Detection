package com.example.palmscanner.data.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.palmscanner.domain.model.HandDetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.abs

class BlurAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {

    companion object {
        private const val EMIT_INTERVAL_MS = 300L
        private val LAPLACIAN_KERNEL = arrayOf(
            intArrayOf( 0, -1,  0),
            intArrayOf(-1,  4, -1),
            intArrayOf( 0, -1,  0)
        )

        private const val SAMPLE_WIDTH  = 160
        private const val SAMPLE_HEIGHT = 120
    }

    private val _blurScoreFlow = MutableStateFlow(0.0)
    val blurScoreFlow: StateFlow<Double> = _blurScoreFlow.asStateFlow()

    private val _isBlurryFlow = MutableStateFlow(false)
    val isBlurryFlow: StateFlow<Boolean> = _isBlurryFlow.asStateFlow()

    private var lastEmitTime = 0L

    override fun analyze(image: ImageProxy) {
        try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmitTime < EMIT_INTERVAL_MS) {
                image.close()
                return
            }
            lastEmitTime = currentTime

            // ── Extract Y plane pixels ──────────────────────────────
            val yPlane   = image.planes[0]
            val yBuffer  = yPlane.buffer
            val rowStride = yPlane.rowStride
            yBuffer.rewind()

            val fullWidth  = image.width
            val fullHeight = image.height

            // ── Downsample for performance ──────────────────────────
            val pixels = downsample(
                yBuffer  = yBuffer,
                rowStride = rowStride,
                srcWidth  = fullWidth,
                srcHeight = fullHeight,
                dstWidth  = SAMPLE_WIDTH,
                dstHeight = SAMPLE_HEIGHT
            )

            // ── Compute Laplacian variance ──────────────────────────
            val variance = computeLaplacianVariance(
                pixels = pixels,
                width  = SAMPLE_WIDTH,
                height = SAMPLE_HEIGHT
            )

            _blurScoreFlow.value = variance
            _isBlurryFlow.value  = variance < HandDetectionResult.BLUR_THRESHOLD

        } finally {
            image.close()
        }
    }

    private fun downsample(
        yBuffer: java.nio.ByteBuffer,
        rowStride: Int,
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int
    ): IntArray {
        val pixels = IntArray(dstWidth * dstHeight)
        val xRatio = srcWidth.toFloat()  / dstWidth
        val yRatio = srcHeight.toFloat() / dstHeight

        for (dstY in 0 until dstHeight) {
            for (dstX in 0 until dstWidth) {
                val srcX = (dstX * xRatio).toInt().coerceIn(0, srcWidth - 1)
                val srcY = (dstY * yRatio).toInt().coerceIn(0, srcHeight - 1)
                val idx  = srcY * rowStride + srcX

                pixels[dstY * dstWidth + dstX] =
                    if (idx < yBuffer.limit())
                        yBuffer[idx].toInt() and 0xFF
                    else 0
            }
        }
        return pixels
    }

    private fun computeLaplacianVariance(
        pixels: IntArray,
        width: Int,
        height: Int
    ): Double {
        val responses = mutableListOf<Int>()

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var response = 0

                // Apply 3x3 Laplacian kernel
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(y + ky) * width + (x + kx)]
                        response += pixel * LAPLACIAN_KERNEL[ky + 1][kx + 1]
                    }
                }

                responses.add(abs(response))
            }
        }

        if (responses.isEmpty()) return 0.0

        // Compute variance of Laplacian responses
        val mean = responses.average()
        val variance = responses.fold(0.0) { acc, v ->
            acc + (v - mean) * (v - mean)
        } / responses.size

        return variance
    }

    fun getCurrentBlurScore(): Double = _blurScoreFlow.value

    fun isCurrentlyBlurry(): Boolean = _isBlurryFlow.value
}