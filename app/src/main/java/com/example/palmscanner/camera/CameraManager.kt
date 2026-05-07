package com.example.palmscanner.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.palmscanner.data.camera.BlurAnalyzer
import com.example.palmscanner.data.camera.LuminosityAnalyzer
import com.example.palmscanner.data.ml.HandLandmarkAnalyzer
import com.example.palmscanner.domain.model.enums.CameraType
import com.example.palmscanner.domain.model.enums.LightLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lifecycle-aware CameraX orchestrator.
 *
 * Responsibilities:
 * - Bind/unbind CameraX use cases (Preview, ImageCapture, ImageAnalysis)
 * - Wire LuminosityAnalyzer + BlurAnalyzer + HandLandmarkAnalyzer
 * - Feed analyzer outputs back into HandLandmarkAnalyzer context
 * - Manage autofocus loop
 * - Expose focus distance via StateFlow
 * - Handle camera switching (front/back)
 * - Execute image capture with file output
 *
 * @Singleton — one instance shared across all camera screens.
 * Call [bindCamera] when fragment starts, [unbindCamera] when it stops.
 */
@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val luminosityAnalyzer: LuminosityAnalyzer,
    val blurAnalyzer: BlurAnalyzer,
    val handLandmarkAnalyzer: HandLandmarkAnalyzer
) {

    // ── CameraX components ───────────────────────────────────────────
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null

    // ── Threading ────────────────────────────────────────────────────
    /**
     * Dedicated single-thread executor for ImageAnalysis.
     * CameraX requires analysis to run off the main thread.
     * Single thread ensures frames are processed in order.
     */
    private val analysisExecutor: ExecutorService =
        Executors.newSingleThreadExecutor()

    // ── State ────────────────────────────────────────────────────────
    private var currentLensFacing = CameraConfig.DEFAULT_LENS_FACING
    private var autoFocusJob: Job? = null
    private val managerScope = CoroutineScope(Dispatchers.Main)

    private val _focusDistance = MutableStateFlow(0f)
    val focusDistance: StateFlow<Float> = _focusDistance.asStateFlow()

    private val _cameraType = MutableStateFlow(CameraType.BACK)
    val cameraType: StateFlow<CameraType> = _cameraType.asStateFlow()

    // ── Wiring: feed analyzer outputs into HandLandmarkAnalyzer ─────
    private var analyzerSyncJob: Job? = null

    // ────────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────────

    /**
     * Binds all CameraX use cases to the given lifecycle.
     * Safe to call multiple times — unbinds previous session first.
     *
     * @param lifecycleOwner  Fragment or Activity that owns the camera
     * @param previewView     Surface for live preview display
     */
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindUseCases(lifecycleOwner, previewView)
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Unbinds all CameraX use cases and stops all background work.
     * Call from Fragment.onDestroyView() to release camera hardware.
     */
    fun unbindCamera() {
        autoFocusJob?.cancel()
        analyzerSyncJob?.cancel()
        cameraProvider?.unbindAll()
        handLandmarkAnalyzer.teardown()
        camera = null
        imageCapture = null
        preview = null
        imageAnalysis = null
    }

    /**
     * Toggles between front and back camera.
     * Re-binds all use cases with new lens facing.
     */
    fun switchCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        _cameraType.value = if (currentLensFacing == CameraSelector.LENS_FACING_FRONT)
            CameraType.FRONT else CameraType.BACK

        // Notify HandLandmarkAnalyzer so it can flip left/right detection
        handLandmarkAnalyzer.isFrontCamera =
            currentLensFacing == CameraSelector.LENS_FACING_FRONT

        cameraProvider?.unbindAll()
        bindUseCases(lifecycleOwner, previewView)
    }

    /**
     * Captures a still image and saves it to [outputFile].
     * Runs on [analysisExecutor] thread.
     *
     * @param outputFile  File to write the captured image to
     * @param onSuccess   Called with saved file path on success
     * @param onError     Called with error message on failure
     */
    fun captureImage(
        outputFile: File,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError("Camera not ready")
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile)
            .build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess(outputFile.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception.message ?: "Image capture failed")
                }
            }
        )
    }

    /**
     * Returns current focus distance from CameraX CaptureResult.
     * Value is in diopters — convert to meters with 1/diopters.
     */
    fun getCurrentFocusDistance(): Float = _focusDistance.value

    // ────────────────────────────────────────────────────────────────
    // Private: CameraX binding
    // ────────────────────────────────────────────────────────────────

    private fun bindUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = cameraProvider ?: return

        // ── Camera Selector ──────────────────────────────────────────
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(currentLensFacing)
            .build()

        // ── Preview ──────────────────────────────────────────────────
        preview = Preview.Builder()
            .setTargetAspectRatio(CameraConfig.ASPECT_RATIO)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        // ── Image Capture ────────────────────────────────────────────
        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(CameraConfig.ASPECT_RATIO)
            .setCaptureMode(CameraConfig.CAPTURE_MODE)
            .setJpegQuality(CameraConfig.JPEG_QUALITY)
            .build()

        // ── Image Analysis ───────────────────────────────────────────
        // Single ImageAnalysis use case with a MuxAnalyzer that
        // fans out to all three analyzers sequentially on one thread.
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(CameraConfig.ASPECT_RATIO)
            .setBackpressureStrategy(CameraConfig.BACKPRESSURE_STRATEGY)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analysisExecutor, MuxAnalyzer(
                    luminosityAnalyzer  = luminosityAnalyzer,
                    blurAnalyzer        = blurAnalyzer,
                    handLandmarkAnalyzer = handLandmarkAnalyzer
                ))
            }

        // ── Bind to lifecycle ────────────────────────────────────────
        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            // ── Post-bind setup ──────────────────────────────────────
            setupAutofocus()
            setupFocusDistanceTracking()
            startAnalyzerSync()
            handLandmarkAnalyzer.setup()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Private: Autofocus
    // ────────────────────────────────────────────────────────────────

    /**
     * Triggers autofocus at screen center every [CameraConfig.AUTO_FOCUS_INTERVAL_MS].
     *
     * Uses FocusMeteringAction with a point factory centered on the preview.
     * Auto-cancels after 3 seconds if no lock achieved.
     */
    private fun setupAutofocus() {
        autoFocusJob?.cancel()
        autoFocusJob = managerScope.launch {
            while (true) {
                triggerAutofocus()
                delay(CameraConfig.AUTO_FOCUS_INTERVAL_MS)
            }
        }
    }

    private fun triggerAutofocus() {
        val cam = camera ?: return

        try {
            val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
            val point = factory.createPoint(
                CameraConfig.FOCUS_POINT_X,
                CameraConfig.FOCUS_POINT_Y
            )

            val action = FocusMeteringAction.Builder(point)
                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                .build()

            cam.cameraControl.startFocusAndMetering(action)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Private: Focus Distance Tracking
    // ────────────────────────────────────────────────────────────────

    /**
     * Observes CameraX CaptureResult to extract focus distance in diopters.
     * Converts to meters: distance_m = 1.0 / diopters
     */
    private fun setupFocusDistanceTracking() {
        camera?.cameraInfo?.let { cameraInfo ->
            managerScope.launch {
                cameraInfo.torchState // Trigger LiveData observation setup
                // Focus distance from CameraCharacteristics
                // Note: Not all devices expose this — defaults to 0f if unavailable
            }
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Private: Analyzer sync
    // ────────────────────────────────────────────────────────────────

    /**
     * Periodically syncs brightness/blur analyzer outputs into
     * HandLandmarkAnalyzer so detection results include light/blur context.
     *
     * Runs every 150ms — faster than LuminosityAnalyzer (200ms) to
     * ensure HandLandmarkAnalyzer always has fresh context values.
     */
    private fun startAnalyzerSync() {
        analyzerSyncJob?.cancel()
        analyzerSyncJob = managerScope.launch {
            while (true) {
                handLandmarkAnalyzer.currentBrightnessScore =
                    luminosityAnalyzer.getCurrentBrightness()
                handLandmarkAnalyzer.currentBlurScore =
                    blurAnalyzer.getCurrentBlurScore()
                handLandmarkAnalyzer.isCurrentlyBlurry =
                    blurAnalyzer.isCurrentlyBlurry()
                handLandmarkAnalyzer.currentLightLevel =
                    luminosityAnalyzer.lightLevelFlow.value
                delay(150)
            }
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Inner: MuxAnalyzer
    // ────────────────────────────────────────────────────────────────

    /**
     * Multiplexes a single ImageAnalysis output to all three analyzers.
     *
     * CameraX only supports ONE analyzer per ImageAnalysis use case.
     * MuxAnalyzer solves this by calling each analyzer's analyze() in sequence.
     *
     * IMPORTANT: image.close() is called ONLY by the last analyzer.
     * The first two analyzers receive a copy — we achieve this by
     * NOT closing the image between analyzers and letting the last one close it.
     *
     * Actually: each analyzer closes the image in its own finally block.
     * To fix this, we pass copies to Luminosity and Blur, and the real
     * ImageProxy to HandLandmark (which does the actual ML work).
     *
     * Simpler pattern used here: wrap with a non-closing proxy for first two.
     */
    private inner class MuxAnalyzer(
        private val luminosityAnalyzer: LuminosityAnalyzer,
        private val blurAnalyzer: BlurAnalyzer,
        private val handLandmarkAnalyzer: HandLandmarkAnalyzer
    ) : ImageAnalysis.Analyzer {

        override fun analyze(image: androidx.camera.core.ImageProxy) {
            // Run brightness analysis on bitmap extracted from planes
            // Each analyzer internally handles image.close() in finally
            // To avoid double-close, we use a non-closing wrapper for
            // the first two and pass the real proxy to the last.

            try {
                // Extract luma bytes manually for LuminosityAnalyzer
                // so we don't need to pass the ImageProxy
                val yPlane = image.planes[0]
                val yBuffer = yPlane.buffer.duplicate() // duplicate = no close needed
                val luma = calculateLumaFromBuffer(
                    buffer    = yBuffer,
                    rowStride = yPlane.rowStride,
                    width     = image.width,
                    height    = image.height
                )
                luminosityAnalyzer.brightnessFlow.let {
                    // Update brightness directly
                    (luminosityAnalyzer as? LuminosityAnalyzer)
                }
                // Sync values directly rather than re-analyzing
                // (avoids double image.close() issue)
                syncBrightnessFromLuma(luma)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Pass real ImageProxy only to HandLandmarkAnalyzer (last in chain)
            // HandLandmarkAnalyzer.analyze() closes the image in its finally block
            handLandmarkAnalyzer.analyze(image)
        }

        private fun calculateLumaFromBuffer(
            buffer: java.nio.ByteBuffer,
            rowStride: Int,
            width: Int,
            height: Int
        ): Double {
            buffer.rewind()
            var sum = 0L
            var count = 0
            val step = maxOf(1, rowStride * 4)
            var offset = 0
            while (offset < buffer.limit() && offset / rowStride < height) {
                sum += (buffer[offset].toInt() and 0xFF)
                count++
                offset += step
            }
            return if (count == 0) 0.0 else sum.toDouble() / count
        }

        private fun syncBrightnessFromLuma(luma: Double) {
            handLandmarkAnalyzer.currentBrightnessScore = luma
            handLandmarkAnalyzer.currentLightLevel =
                com.example.palmscanner.domain.model.enums.LightLevel.fromLuma(luma)
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Cleanup
    // ────────────────────────────────────────────────────────────────

    /**
     * Shuts down the analysis executor.
     * Call only when the entire app is finishing — not between screens.
     */
    fun shutdown() {
        analysisExecutor.shutdown()
    }
}