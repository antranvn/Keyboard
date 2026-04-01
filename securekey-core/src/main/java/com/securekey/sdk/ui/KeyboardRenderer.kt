package com.securekey.sdk.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.securekey.sdk.core.Key
import com.securekey.sdk.core.KeyType
import com.securekey.sdk.core.KeyboardLayout

/**
 * Renders the keyboard on a Canvas with Gboard-style visuals.
 * Rounded key shapes, subtle shadows, distinct modifier/action styling.
 */
class KeyboardRenderer {

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
    }

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

    fun setDensity(d: Float) {
        density = d
    }

    /** Set the currently pressed key for visual feedback */
    fun setPressedKey(key: Key?) {
        pressedKey = key
    }

    /** Draw the entire keyboard */
    fun draw(canvas: Canvas, layout: KeyboardLayout) {
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
                density,
                Color.parseColor("#20000000")
            )
        } else {
            keyPaint.clearShadowLayer()
        }

        canvas.drawRoundRect(drawBounds, keyCornerRadius, keyCornerRadius, keyPaint)

        // Key label
        textPaint.color = when {
            isAction -> actionKeyTextColor
            isModifier -> modifierKeyTextColor
            else -> keyTextColor
        }

        val isSpecialLabel = key.value == "SHIFT" || key.value == "BACKSPACE"

        textPaint.textSize = when {
            isSpecialLabel -> keyTextSize * 1.1f
            key.type == KeyType.ACTION -> keyTextSize * 0.75f
            key.type == KeyType.MODIFIER -> keyTextSize * 0.7f
            key.type == KeyType.NUMERIC -> keyTextSize * 0.85f
            else -> keyTextSize
        }

        textPaint.typeface = when {
            key.type == KeyType.ACTION -> Typeface.create("sans-serif-medium", Typeface.BOLD)
            key.type == KeyType.MODIFIER -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            else -> Typeface.create("sans-serif", Typeface.NORMAL)
        }

        val textX = drawBounds.centerX()
        val textY = drawBounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(key.label, textX, textY, textPaint)

        keyPaint.clearShadowLayer()
    }
}
