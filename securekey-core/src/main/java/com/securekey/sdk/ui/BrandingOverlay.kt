package com.securekey.sdk.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Renders bank logo and "Secured by SecureKey" badge on the keyboard.
 */
class BrandingOverlay {

    enum class LogoPosition { TOP_LEFT, CENTER, TOP_RIGHT }
    enum class BrandingMode { LOGO_BAR, WATERMARK }

    var bankLogo: Bitmap? = null
    var logoPosition: LogoPosition = LogoPosition.TOP_LEFT
    var brandingMode: BrandingMode = BrandingMode.LOGO_BAR
    var watermarkAlpha: Float = 0.08f
    var showSecuredByBadge: Boolean = true

    private val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    /** Height of the branding bar in pixels */
    val barHeight: Float
        get() = if (brandingMode == BrandingMode.LOGO_BAR && bankLogo != null) 80f else 0f

    /** Draw the branding overlay */
    fun draw(canvas: Canvas, keyboardBounds: RectF, density: Float) {
        val logo = bankLogo

        when (brandingMode) {
            BrandingMode.LOGO_BAR -> {
                if (logo != null) {
                    drawLogoBar(canvas, logo, keyboardBounds, density)
                }
            }
            BrandingMode.WATERMARK -> {
                if (logo != null) {
                    drawWatermark(canvas, logo, keyboardBounds)
                }
            }
        }

        if (showSecuredByBadge) {
            drawSecuredByBadge(canvas, keyboardBounds, density)
        }
    }

    private fun drawLogoBar(canvas: Canvas, logo: Bitmap, bounds: RectF, density: Float) {
        val maxWidth = 120 * density
        val maxHeight = 32 * density
        val scale = minOf(maxWidth / logo.width, maxHeight / logo.height)
        val scaledWidth = logo.width * scale
        val scaledHeight = logo.height * scale

        val x = when (logoPosition) {
            LogoPosition.TOP_LEFT -> bounds.left + 16 * density
            LogoPosition.CENTER -> bounds.centerX() - scaledWidth / 2
            LogoPosition.TOP_RIGHT -> bounds.right - scaledWidth - 16 * density
        }
        val y = bounds.top + (barHeight - scaledHeight) / 2

        val destRect = RectF(x, y, x + scaledWidth, y + scaledHeight)
        canvas.drawBitmap(logo, null, destRect, logoPaint)
    }

    private fun drawWatermark(canvas: Canvas, logo: Bitmap, bounds: RectF) {
        watermarkPaint.alpha = (watermarkAlpha * 255).toInt()
        val scale = minOf(bounds.width() * 0.4f / logo.width, bounds.height() * 0.4f / logo.height)
        val scaledWidth = logo.width * scale
        val scaledHeight = logo.height * scale
        val x = bounds.centerX() - scaledWidth / 2
        val y = bounds.centerY() - scaledHeight / 2

        val destRect = RectF(x, y, x + scaledWidth, y + scaledHeight)
        canvas.drawBitmap(logo, null, destRect, watermarkPaint)
    }

    private fun drawSecuredByBadge(canvas: Canvas, bounds: RectF, density: Float) {
        badgePaint.textSize = 10 * density
        badgePaint.color = 0x80808080.toInt()
        val text = "Secured by SecureKey"
        canvas.drawText(text, bounds.centerX(), bounds.bottom - 4 * density, badgePaint)
    }
}
