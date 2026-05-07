package com.example.palmscanner.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class FingerOvalOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var isDetected  = false
    private var fingerLabel = "FINGER"

    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99000000")
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeWidth = 6f
        color       = Color.WHITE
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.WHITE
        textSize  = 42f
        textAlign = Paint.Align.CENTER
    }

    private val ovalRect = RectF()

    init {
        // CRITICAL: software layer needed for PorterDuff.CLEAR to work
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        // Make view background transparent
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun updateState(detected: Boolean, label: String) {
        isDetected  = detected
        fingerLabel = label
        borderPaint.color = if (detected) Color.GREEN else Color.WHITE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val cx = width / 2f
        val cy = height / 2f
        val rx = width * 0.22f
        val ry = height * 0.35f

        ovalRect.set(cx - rx, cy - ry, cx + rx, cy + ry)

        // Step 1: Draw dark scrim over entire view
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)

        // Step 2: Cut transparent hole in scrim at oval position
        // This lets camera preview show through the oval
        canvas.drawOval(ovalRect, clearPaint)

        // Step 3: Draw oval border on top
        canvas.drawOval(ovalRect, borderPaint)

        // Step 4: Draw finger label below oval
        canvas.drawText(
            fingerLabel,
            cx,
            ovalRect.bottom + 60f,
            labelPaint
        )
    }
}