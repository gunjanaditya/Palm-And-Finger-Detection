package com.example.palmscanner.camera

import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture

/**
 * Central configuration for all CameraX use cases.
 * Change values here to affect the entire camera pipeline.
 *
 * Kept as an object (singleton) since config never changes at runtime.
 */
object CameraConfig {

    // ── Resolution ───────────────────────────────────────────────────
    /**
     * RATIO_4_3 fills more of a portrait screen than 16:9.
     * Better for palm scanning since hands are roughly square.
     */
    const val ASPECT_RATIO = AspectRatio.RATIO_4_3

    // ── Image Capture ────────────────────────────────────────────────
    /**
     * CAPTURE_MODE_MINIMIZE_LATENCY → faster capture, slight quality tradeoff.
     * For biometric scanning we want responsive capture, not maximum quality.
     */
    const val CAPTURE_MODE = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY

    /**
     * JPEG quality for finger captures (0–100).
     * 90 gives excellent quality with manageable file size.
     */
    const val JPEG_QUALITY = 90

    // ── Image Analysis ───────────────────────────────────────────────
    /**
     * STRATEGY_KEEP_ONLY_LATEST drops frames if analyzer is busy.
     * Prevents backpressure buildup — we never want stale frames
     * queued up behind the current one.
     */
    const val BACKPRESSURE_STRATEGY = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST

    // ── Analysis Resolution ──────────────────────────────────────────
    /**
     * 640x480 for analysis — enough for MediaPipe accuracy,
     * low enough for real-time processing without frame drops.
     * Actual capture uses full sensor resolution.
     */
    const val ANALYSIS_WIDTH  = 640
    const val ANALYSIS_HEIGHT = 480

    // ── Autofocus ────────────────────────────────────────────────────
    /**
     * Focus point at center of preview (normalized 0.0–1.0).
     * Palm is always expected to be centered in frame.
     */
    const val FOCUS_POINT_X = 0.5f
    const val FOCUS_POINT_Y = 0.5f

    /**
     * Re-trigger autofocus every N milliseconds to track
     * hand movement without user intervention.
     */
    const val AUTO_FOCUS_INTERVAL_MS = 2000L

    // ── Camera Selection ─────────────────────────────────────────────
    /**
     * Default to back camera for palm scanning.
     * Front camera available as fallback via CameraManager toggle.
     */
    const val DEFAULT_LENS_FACING = androidx.camera.core.CameraSelector.LENS_FACING_BACK
}