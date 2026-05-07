package com.example.palmscanner.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.palmscanner.domain.model.HandDetectionResult
import com.example.palmscanner.domain.model.enums.FingerName
import com.example.palmscanner.domain.model.enums.HandSide
import com.example.palmscanner.domain.model.enums.LightLevel
import com.example.palmscanner.domain.model.enums.PalmSide
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HandLandmarkAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fingerExtensionChecker: FingerExtensionChecker,
    private val palmSideDetector: PalmSideDetector
) : ImageAnalysis.Analyzer {

    companion object {
        private const val MODEL_FILE = "hand_landmarker.task"
        private const val MAX_HANDS  = 1       // Detect only 1 hand at a time
        private const val MIN_DETECTION_CONFIDENCE  = 0.6f
        private const val MIN_TRACKING_CONFIDENCE   = 0.5f
        private const val MIN_PRESENCE_CONFIDENCE   = 0.5f
        private const val EMIT_INTERVAL_MS = 100L  // Max 10 results/sec to ViewModel
    }

    // ── Flows consumed by repositories and ViewModels ───────────────
    private val _detectionResultFlow = MutableStateFlow(HandDetectionResult.empty())
    val detectionResultFlow: StateFlow<HandDetectionResult> =
        _detectionResultFlow.asStateFlow()

    // ── External inputs set by CameraManager ────────────────────────
    var currentBrightnessScore: Double = 0.0
    var currentBlurScore: Double       = 0.0
    var isCurrentlyBlurry: Boolean     = false
    var currentLightLevel: LightLevel  = LightLevel.NORMAL
    var isFrontCamera: Boolean         = false

    private var handLandmarker: HandLandmarker? = null
    private var lastEmitTime = 0L
    private var isRunning    = false

    fun setup() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_FILE)
                .setDelegate(Delegate.GPU)   // Falls back to CPU automatically
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(MAX_HANDS)
                .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setMinHandPresenceConfidence(MIN_PRESENCE_CONFIDENCE)
                .setResultListener { result, _ -> processResult(result) }
                .setErrorListener { error ->
                    _detectionResultFlow.value = HandDetectionResult.empty()
                }
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            isRunning = true

        } catch (e: Exception) {
            e.printStackTrace()
            isRunning = false
        }
    }

    fun teardown() {
        isRunning = false
        handLandmarker?.close()
        handLandmarker = null
    }

    override fun analyze(image: ImageProxy) {
        if (!isRunning || handLandmarker == null) {
            image.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmitTime < EMIT_INTERVAL_MS) {
            image.close()
            return
        }
        lastEmitTime = currentTime

        try {
            // ── Convert ImageProxy → Bitmap ─────────────────────────
            val bitmap = imageProxyToBitmap(image)

            // ── Wrap in MediaPipe MPImage ───────────────────────────
            val mpImage = BitmapImageBuilder(bitmap).build()

            // ── Send to MediaPipe (LIVE_STREAM: result via callback) ─
            handLandmarker?.detectAsync(mpImage, currentTime)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }

    private fun processResult(result: HandLandmarkerResult) {
        // No hands detected
        if (result.landmarks().isEmpty()) {
            _detectionResultFlow.value = HandDetectionResult(
                isDetected       = false,
                lightLevel       = currentLightLevel,
                brightnessScore  = currentBrightnessScore,
                blurScore        = currentBlurScore,
                isBlurry         = isCurrentlyBlurry
            )
            return
        }

        // ── Get first hand's landmarks (21 points) ──────────────────
        val landmarks = result.landmarks()[0]

        // ── Hand side from MediaPipe ─────────────────────────────────
        // MediaPipe returns "Left"/"Right" from its perspective (mirrored on front cam)
        val rawHandedness = result.handednesses()
            .firstOrNull()
            ?.firstOrNull()
            ?.categoryName() ?: "Unknown"

        val handSide = resolveHandSide(rawHandedness)

        // ── Palm side detection ──────────────────────────────────────
        val palmSide = palmSideDetector.detect(landmarks, handSide)

        // ── Finger extension detection ───────────────────────────────
        val fingersExtended = fingerExtensionChecker.checkExtension(
            landmarks = landmarks,
            handSide  = handSide,
            isFrontCamera = isFrontCamera
        )
        val fingerCount = fingersExtended.count { it }

        // ── Detect which single finger is isolated (for finger screen) ─
        val detectedFinger = detectSingleFinger(fingersExtended)

        // ── Emit result ──────────────────────────────────────────────
        _detectionResultFlow.value = HandDetectionResult(
            isDetected          = true,
            handSide            = handSide,
            palmSide            = palmSide,
            fingersExtended     = fingersExtended,
            fingerCount         = fingerCount,
            lightLevel          = currentLightLevel,
            brightnessScore     = currentBrightnessScore,
            blurScore           = currentBlurScore,
            isBlurry            = isCurrentlyBlurry,
            detectedFingerName  = detectedFinger
        )
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val bitmap = imageProxy.toBitmap()
        val rotation = imageProxy.imageInfo.rotationDegrees

        return if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                .also { if (it != bitmap) bitmap.recycle() }
        } else {
            bitmap
        }
    }

    private fun resolveHandSide(rawHandedness: String): HandSide {
        val mediapipeSide = when (rawHandedness.lowercase()) {
            "left"  -> HandSide.LEFT
            "right" -> HandSide.RIGHT
            else    -> HandSide.UNKNOWN
        }

        // Mirror for front camera
        return if (isFrontCamera) {
            when (mediapipeSide) {
                HandSide.LEFT  -> HandSide.RIGHT
                HandSide.RIGHT -> HandSide.LEFT
                HandSide.UNKNOWN -> HandSide.UNKNOWN
            }
        } else {
            mediapipeSide
        }
    }

    private fun detectSingleFinger(fingersExtended: List<Boolean>): FingerName? {
        val extendedCount = fingersExtended.count { it }
        if (extendedCount != 1) return null

        return when (fingersExtended.indexOfFirst { it }) {
            0 -> FingerName.THUMB
            1 -> FingerName.INDEX
            2 -> FingerName.MIDDLE
            3 -> FingerName.RING
            4 -> FingerName.LITTLE
            else -> null
        }
    }
}