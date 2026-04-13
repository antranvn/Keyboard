package com.securekey.sdk.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.animation.OvershootInterpolator
import com.securekey.sdk.core.Key
import com.securekey.sdk.core.KeyType

/**
 * Gboard-style key preview bubble shown above the pressed key.
 * Rounded rectangle with a pointer/stem extending down to the key.
 */
class KeyPreviewPopup {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    private val stemPath = Path()
    private val interpolator = OvershootInterpolator(1.2f)

    private var activeKey: Key? = null
    private var animationStartTime: Long = 0L

    var popupOffsetDp: Float = 12f
    /** Fixed popup width in dp — consistent across all keys */
    var popupWidthDp: Float = 52f
    /** Fixed popup height in dp — consistent across all keys */
    var popupHeightDp: Float = 56f
    var animationDurationMs: Long = 80L
    var backgroundColor: Int = Color.parseColor("#616161")
    var textColor: Int = Color.WHITE
    var cornerRadius: Float = 20f
    var textSize: Float = 56f

    /** Show preview for the given key */
    fun show(key: Key) {
        // Only show for character/numeric keys, not for modifiers or actions
        if (key.type == KeyType.MODIFIER || key.type == KeyType.ACTION || key.type == KeyType.SPECIAL) {
            activeKey = null
            return
        }
        activeKey = key
        animationStartTime = System.currentTimeMillis()
    }

    /** Hide the preview */
    fun hide() {
        activeKey = null
    }

    /**
     * Draw the popup on the canvas.
     * Returns true if the animation is still in progress and another frame is needed.
     */
    fun draw(canvas: Canvas, density: Float): Boolean {
        val key = activeKey ?: return false

        val keyBounds = key.bounds
        val popupWidth = popupWidthDp * density
        val popupHeight = popupHeightDp * density
        val stemHeight = 8f * density
        val offsetPx = popupOffsetDp * density
        val centerX = keyBounds.centerX()

        // Popup body position (above key)
        val bodyBottom = keyBounds.top - offsetPx
        val bodyTop = bodyBottom - popupHeight
        val bodyLeft = centerX - popupWidth / 2
        val bodyRight = centerX + popupWidth / 2

        val bodyBounds = RectF(bodyLeft, bodyTop, bodyRight, bodyBottom)

        // Clamp to canvas edges
        if (bodyBounds.left < 4f * density) {
            bodyBounds.offset(4f * density - bodyBounds.left, 0f)
        }
        if (bodyBounds.right > canvas.width - 4f * density) {
            bodyBounds.offset(canvas.width - 4f * density - bodyBounds.right, 0f)
        }
        if (bodyBounds.top < 0f) {
            bodyBounds.offset(0f, -bodyBounds.top)
        }

        // Animate scale with overshoot for a snappy pop-in feel
        val elapsed = System.currentTimeMillis() - animationStartTime
        val animating: Boolean
        val scale: Float
        if (animationDurationMs > 0 && elapsed < animationDurationMs) {
            val progress = elapsed.toFloat() / animationDurationMs
            scale = interpolator.getInterpolation(progress)
            animating = true
        } else {
            scale = 1f
            animating = false
        }

        canvas.save()
        canvas.scale(scale, scale, bodyBounds.centerX(), bodyBounds.bottom + stemHeight)

        // Draw bubble body
        paint.color = backgroundColor
        paint.setShadowLayer(6f * density, 0f, 2f * density, Color.parseColor("#40000000"))
        canvas.drawRoundRect(bodyBounds, cornerRadius, cornerRadius, paint)
        paint.clearShadowLayer()

        // Draw small stem triangle pointing down
        stemPath.reset()
        stemPath.moveTo(bodyBounds.centerX() - 8f * density, bodyBounds.bottom)
        stemPath.lineTo(bodyBounds.centerX() + 8f * density, bodyBounds.bottom)
        stemPath.lineTo(bodyBounds.centerX(), bodyBounds.bottom + stemHeight)
        stemPath.close()
        canvas.drawPath(stemPath, paint)

        // Draw label text
        textPaint.color = textColor
        textPaint.textSize = textSize
        val textY = bodyBounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(key.label, bodyBounds.centerX(), textY, textPaint)

        canvas.restore()
        return animating
    }

    /** Whether the popup is currently visible */
    val isVisible: Boolean get() = activeKey != null
}
