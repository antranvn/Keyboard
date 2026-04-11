package com.securekey.sdk.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.securekey.sdk.core.Key
import com.securekey.sdk.core.KeyType
import com.securekey.sdk.core.KeyboardLayout
import com.securekey.sdk.core.KeyboardMode

/**
 * Renders the keyboard on a Canvas with Gboard-style visuals.
 * Rounded key shapes, subtle shadows, distinct modifier/action styling.
 * Draws Path-based icons for Shift and Backspace keys.
 */
class KeyboardRenderer {

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val iconPath = Path()

    // Character keys (letters, numbers, punctuation)
    var keyBackgroundColor: Int = Color.WHITE
    var keyPressedColor: Int = Color.parseColor("#D4D6DA")
    var keyTextColor: Int = Color.parseColor("#1B1B1F")

    // Keyboard background
    var keyboardBackgroundColor: Int = Color.parseColor("#ECEFF1")

    // Action keys (Done/Enter)
    var actionKeyColor: Int = Color.parseColor("#4285F4")
    var actionKeyTextColor: Int = Color.WHITE

    // Modifier keys (Shift, Backspace, ?123, ABC)
    var modifierKeyColor: Int = Color.parseColor("#B8BCC2")
    var modifierKeyTextColor: Int = Color.parseColor("#1B1B1F")
    var modifierKeyPressedColor: Int = Color.parseColor("#9EA2A8")

    // Space bar
    var spaceBarColor: Int = Color.WHITE

    var keyCornerRadius: Float = 16f
    var keyTextSize: Float = 48f
    var keyElevation: Float = 1.5f
    var keyStrokeColor: Int = Color.TRANSPARENT

    private var pressedKey: Key? = null
    private var density: Float = 1f
    private var currentMode: KeyboardMode? = null

    fun setDensity(d: Float) {
        density = d
        iconPaint.strokeWidth = 1.8f * d
    }

    /** Set the currently pressed key for visual feedback */
    fun setPressedKey(key: Key?) {
        pressedKey = key
    }

    /** Draw the entire keyboard */
    fun draw(canvas: Canvas, layout: KeyboardLayout) {
        currentMode = layout.mode

        // Draw background
        backgroundPaint.color = keyboardBackgroundColor
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)

        // Draw each key
        for (row in layout.rows) {
            for (key in row) {
                drawKey(canvas, key)
            }
        }
    }

    private fun drawKey(canvas: Canvas, key: Key) {
        val bounds = key.bounds
        if (bounds.isEmpty) return

        val isPressed = key == pressedKey
        val isAction = key.type == KeyType.ACTION
        val isModifier = key.type == KeyType.MODIFIER
        val isSpace = key.type == KeyType.SPECIAL && key.value == " "

        // Inset bounds slightly for visual gap
        val inset = 2f * density
        val drawBounds = RectF(
            bounds.left + inset,
            bounds.top + inset,
            bounds.right - inset,
            bounds.bottom - inset
        )

        // Determine key color
        keyPaint.color = when {
            isPressed && isModifier -> modifierKeyPressedColor
            isPressed -> keyPressedColor
            isAction -> actionKeyColor
            isModifier -> modifierKeyColor
            isSpace -> spaceBarColor
            else -> keyBackgroundColor
        }

        // Shadow for character keys only (subtle)
        if (keyElevation > 0 && !isPressed && !isModifier) {
            keyPaint.setShadowLayer(
                keyElevation * density,
                0f,
                density * 0.5f,
                Color.parseColor("#18000000")
            )
        } else {
            keyPaint.clearShadowLayer()
        }

        canvas.drawRoundRect(drawBounds, keyCornerRadius, keyCornerRadius, keyPaint)
        keyPaint.clearShadowLayer()

        // Draw icon or text label
        when (key.value) {
            "SHIFT" -> drawShiftIcon(canvas, drawBounds, isModifier)
            "BACKSPACE" -> drawBackspaceIcon(canvas, drawBounds, isModifier, isAction)
            else -> drawTextLabel(canvas, key, drawBounds, isAction, isModifier)
        }
    }

    /** Draw shift arrow icon (like Gboard) */
    private fun drawShiftIcon(canvas: Canvas, bounds: RectF, isModifier: Boolean) {
        val iconColor = if (isModifier) modifierKeyTextColor else keyTextColor
        val iconSize = 20f * density
        val cx = bounds.centerX()
        val cy = bounds.centerY()

        iconPath.reset()
        // Arrow head pointing up
        val arrowTop = cy - iconSize * 0.5f
        val arrowMid = cy - iconSize * 0.05f
        val arrowWidth = iconSize * 0.55f
        val stemWidth = iconSize * 0.22f
        val stemBottom = cy + iconSize * 0.45f

        iconPath.moveTo(cx, arrowTop)                         // Top point
        iconPath.lineTo(cx + arrowWidth, arrowMid)            // Right of arrow
        iconPath.lineTo(cx + stemWidth, arrowMid)             // Right inner
        iconPath.lineTo(cx + stemWidth, stemBottom)            // Bottom right
        iconPath.lineTo(cx - stemWidth, stemBottom)            // Bottom left
        iconPath.lineTo(cx - stemWidth, arrowMid)             // Left inner
        iconPath.lineTo(cx - arrowWidth, arrowMid)            // Left of arrow
        iconPath.close()

        // Check if shift is active (label is uppercase indicator)
        // Filled when active, outline when inactive
        iconFillPaint.color = iconColor
        iconFillPaint.style = Paint.Style.FILL
        canvas.drawPath(iconPath, iconFillPaint)

        // Draw outline too for cleaner look
        iconPaint.color = iconColor
        iconPaint.strokeWidth = 1.5f * density
        canvas.drawPath(iconPath, iconPaint)
    }

    /** Draw backspace icon (rounded rectangle with X, like Gboard) */
    private fun drawBackspaceIcon(canvas: Canvas, bounds: RectF, isModifier: Boolean, isAction: Boolean) {
        val iconColor = when {
            isAction -> actionKeyTextColor
            isModifier -> modifierKeyTextColor
            else -> keyTextColor
        }
        val iconSize = 20f * density
        val cx = bounds.centerX()
        val cy = bounds.centerY()

        // Draw the backspace shape (rounded rect with left arrow point)
        val left = cx - iconSize * 0.55f
        val right = cx + iconSize * 0.5f
        val top = cy - iconSize * 0.38f
        val bottom = cy + iconSize * 0.38f
        val pointX = left - iconSize * 0.25f

        iconPath.reset()
        val cornerR = 3f * density
        // Start from top-right with rounded corner
        iconPath.moveTo(right - cornerR, top)
        iconPath.lineTo(right, top + cornerR) // top-right corner (simplified)
        iconPath.moveTo(right - cornerR, top)
        // Top edge
        iconPath.addRoundRect(
            RectF(left, top, right, bottom),
            cornerR, cornerR,
            Path.Direction.CW
        )
        iconPath.reset()

        // Backspace outline shape
        iconPath.moveTo(right - cornerR, top)
        iconPath.quadTo(right, top, right, top + cornerR)
        iconPath.lineTo(right, bottom - cornerR)
        iconPath.quadTo(right, bottom, right - cornerR, bottom)
        iconPath.lineTo(left, bottom)
        iconPath.lineTo(pointX, cy)
        iconPath.lineTo(left, top)
        iconPath.close()

        iconPaint.color = iconColor
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 1.5f * density
        canvas.drawPath(iconPath, iconPaint)

        // Draw X inside
        val xSize = iconSize * 0.18f
        val xCx = cx + iconSize * 0.05f
        iconPaint.strokeWidth = 1.8f * density
        canvas.drawLine(xCx - xSize, cy - xSize, xCx + xSize, cy + xSize, iconPaint)
        canvas.drawLine(xCx - xSize, cy + xSize, xCx + xSize, cy - xSize, iconPaint)
    }

    private fun drawTextLabel(canvas: Canvas, key: Key, drawBounds: RectF, isAction: Boolean, isModifier: Boolean) {
        textPaint.color = when {
            isAction -> actionKeyTextColor
            isModifier -> modifierKeyTextColor
            else -> keyTextColor
        }

        // In pad-style layouts, keys are large — bump every label accordingly
        // so Done/Paste/etc don't look tiny next to the big digits.
        val isNumericPad = currentMode == KeyboardMode.NUMERIC_PIN ||
            currentMode == KeyboardMode.NUMERIC_OTP ||
            currentMode == KeyboardMode.AMOUNT_PAD
        textPaint.textSize = when (key.type) {
            KeyType.ACTION -> if (isNumericPad) keyTextSize * 0.85f else keyTextSize * 0.65f
            KeyType.MODIFIER -> keyTextSize * 0.6f
            KeyType.NUMERIC -> if (isNumericPad) keyTextSize * 1.4f else keyTextSize * 0.8f
            else -> if (isNumericPad) keyTextSize * 0.85f else keyTextSize * 0.9f
        }

        textPaint.typeface = when {
            key.type == KeyType.ACTION -> Typeface.create("sans-serif-medium", Typeface.BOLD)
            key.type == KeyType.MODIFIER -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            else -> Typeface.create("sans-serif", Typeface.NORMAL)
        }

        val textX = drawBounds.centerX()
        val textY = drawBounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(key.label, textX, textY, textPaint)
    }
}
