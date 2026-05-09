package com.example.palmscanner.data.ml

import com.example.palmscanner.domain.model.enums.HandSide
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject

class FingerExtensionChecker @Inject constructor() {

    companion object {
        // Landmark indices
        private const val WRIST       = 0

        private const val THUMB_CMC   = 1
        private const val THUMB_MCP   = 2
        private const val THUMB_IP    = 3
        private const val THUMB_TIP   = 4

        private const val INDEX_MCP   = 5
        private const val INDEX_PIP   = 6
        private const val INDEX_TIP   = 8

        private const val MIDDLE_MCP  = 9
        private const val MIDDLE_PIP  = 10
        private const val MIDDLE_TIP  = 12

        private const val RING_MCP    = 13
        private const val RING_PIP    = 14
        private const val RING_TIP    = 16

        private const val LITTLE_MCP  = 17
        private const val LITTLE_PIP  = 18
        private const val LITTLE_TIP  = 20

        private const val EXTENSION_MARGIN = 0.02f
    }

    fun checkExtension(
        landmarks: List<NormalizedLandmark>,
        handSide: HandSide,
        isFrontCamera: Boolean = false
    ): List<Boolean> {
        if (landmarks.size < 21) return List(5) { false }

        return listOf(
            isThumbExtended(landmarks, handSide, isFrontCamera),
            isIndexExtended(landmarks),
            isMiddleExtended(landmarks),
            isRingExtended(landmarks),
            isLittleExtended(landmarks)
        )
    }

    private fun isThumbExtended(
        landmarks: List<NormalizedLandmark>,
        handSide: HandSide,
        isFrontCamera: Boolean
    ): Boolean {
        val tip = landmarks[THUMB_TIP]
        val ip  = landmarks[THUMB_IP]

        // Distance from tip to IP joint must be meaningful
        var horizontalDiff = tip.x() - ip.x()

        // If front camera, the screen image is mirrored.
        // A physical left-hand thumb moves left, but on screen it moves right.
        if (isFrontCamera) {
            horizontalDiff = -horizontalDiff
        }

        return when (handSide) {
            HandSide.RIGHT -> horizontalDiff > EXTENSION_MARGIN
            HandSide.LEFT  -> horizontalDiff < -EXTENSION_MARGIN
            HandSide.UNKNOWN -> {
                // Fallback: use absolute distance
                Math.abs(horizontalDiff) > EXTENSION_MARGIN
            }
        }
    }

    private fun isIndexExtended(landmarks: List<NormalizedLandmark>): Boolean {
        val tip = landmarks[INDEX_TIP]
        val pip = landmarks[INDEX_PIP]
        return (pip.y() - tip.y()) > EXTENSION_MARGIN
    }
    private fun isMiddleExtended(landmarks: List<NormalizedLandmark>): Boolean {
        val tip = landmarks[MIDDLE_TIP]
        val pip = landmarks[MIDDLE_PIP]
        return (pip.y() - tip.y()) > EXTENSION_MARGIN
    }

    private fun isRingExtended(landmarks: List<NormalizedLandmark>): Boolean {
        val tip = landmarks[RING_TIP]
        val pip = landmarks[RING_PIP]
        return (pip.y() - tip.y()) > EXTENSION_MARGIN
    }

    private fun isLittleExtended(landmarks: List<NormalizedLandmark>): Boolean {
        val tip = landmarks[LITTLE_TIP]
        val pip = landmarks[LITTLE_PIP]
        return (pip.y() - tip.y()) > EXTENSION_MARGIN
    }

}