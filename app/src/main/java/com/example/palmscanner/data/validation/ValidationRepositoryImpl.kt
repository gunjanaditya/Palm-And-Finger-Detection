package com.example.palmscanner.data.validation

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.ValidationResult
import com.example.palmscanner.domain.model.ValidationFailureReason
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.repository.IValidationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ValidationRepositoryImpl @Inject constructor() : IValidationRepository {

    companion object {
        private const val MATCH_THRESHOLD = 0.50f
        private const val SAMPLE_SIZE = 64
    }

    // Stores palm feature vector after registerPalm()
    private var palmFeatures: FloatArray? = null
    private var registeredHandSide: HandSide = HandSide.UNKNOWN

    override suspend fun registerPalm(
        palmBitmap: Bitmap,
        handSide: HandSide
    ) = withContext(Dispatchers.Default) {
        palmFeatures = extractFeatures(palmBitmap)
        registeredHandSide = handSide
    }

    override suspend fun validateFinger(
        fingerBitmap: Bitmap,
        expectedHand: HandSide
    ): ValidationResult = withContext(Dispatchers.Default) {

        // Guard: wrong hand side
        if (registeredHandSide != HandSide.UNKNOWN &&
            expectedHand != HandSide.UNKNOWN &&
            registeredHandSide != expectedHand
        ) {
            return@withContext ValidationResult(
                isMatch       = false,
                score         = 0f,
                failureReason = ValidationFailureReason.WRONG_HAND
            )
        }

        // Guard: no palm registered
        val palmVec = palmFeatures ?: return@withContext ValidationResult(
            isMatch       = false,
            score         = 0f,
            failureReason = ValidationFailureReason.NO_HAND_DETECTED
        )

        // Extract finger features and compute score
        val fingerVec = extractFeatures(fingerBitmap)
        val score     = computeMatchScore(palmVec, fingerVec)
        val isMatch   = score >= MATCH_THRESHOLD

        ValidationResult(
            isMatch       = isMatch,
            score         = score,
            failureReason = if (isMatch) null else ValidationFailureReason.DIFFERENT_PERSON
        )
    }

    override fun clearSession() {
        palmFeatures      = null
        registeredHandSide = HandSide.UNKNOWN
    }

    private fun extractFeatures(bitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, SAMPLE_SIZE, SAMPLE_SIZE, true)
        val half   = SAMPLE_SIZE / 2

        val quadrants = listOf(
            Pair(0 until half,      0 until half),       // top-left
            Pair(0 until half,      half until SAMPLE_SIZE), // top-right
            Pair(half until SAMPLE_SIZE, 0 until half),  // bottom-left
            Pair(half until SAMPLE_SIZE, half until SAMPLE_SIZE) // bottom-right
        )

        return FloatArray(quadrants.size) { i ->
            val (rows, cols) = quadrants[i]
            var sum = 0L
            var count = 0
            for (y in rows) {
                for (x in cols) {
                    val pixel = scaled.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8)  and 0xFF
                    val b =  pixel         and 0xFF
                    sum += (r + g + b) / 3
                    count++
                }
            }
            if (count == 0) 0f else sum.toFloat() / count
        }
    }

    private fun computeMatchScore(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        val maxDiff = 255f
        val avgDiff = a.zip(b.toList()).map { (x, y) -> abs(x - y) }.average().toFloat()
        return (1f - (avgDiff / maxDiff)).coerceIn(0f, 1f)
    }
}