package com.example.palmscanner.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.palmscanner.R

class PalmOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var isDetected = false
    private var isReady    = false

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeWidth = 6f
        color       = Color.WHITE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#33FFFFFF")
    }

    private val overlayRect = RectF()

    fun updateState(detected: Boolean, ready: Boolean) {
        isDetected = detected
        isReady    = ready
        borderPaint.color = when {
            isReady    -> Color.GREEN
            isDetected -> Color.YELLOW
            else       -> Color.WHITE
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx     = width / 2f
        val cy     = height / 2f
        val rw     = width * 0.38f
        val rh     = height * 0.45f

        overlayRect.set(cx - rw, cy - rh, cx + rw, cy + rh)

        // Draw filled rounded rect
        canvas.drawRoundRect(overlayRect, 40f, 40f, fillPaint)
        // Draw border
        canvas.drawRoundRect(overlayRect, 40f, 40f, borderPaint)

        // Draw corner guides
        drawCornerGuides(canvas, overlayRect)
    }

    private fun drawCornerGuides(canvas: Canvas, rect: RectF) {
        val len = 40f
        val p   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style       = Paint.Style.STROKE
            strokeWidth = 8f
            color       = borderPaint.color
        }
        // Top-left
        canvas.drawLine(rect.left, rect.top + len, rect.left, rect.top, p)
        canvas.drawLine(rect.left, rect.top, rect.left + len, rect.top, p)
        // Top-right
        canvas.drawLine(rect.right - len, rect.top, rect.right, rect.top, p)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + len, p)
        // Bottom-left
        canvas.drawLine(rect.left, rect.bottom - len, rect.left, rect.bottom, p)
        canvas.drawLine(rect.left, rect.bottom, rect.left + len, rect.bottom, p)
        // Bottom-right
        canvas.drawLine(rect.right - len, rect.bottom, rect.right, rect.bottom, p)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - len, p)
    }
}