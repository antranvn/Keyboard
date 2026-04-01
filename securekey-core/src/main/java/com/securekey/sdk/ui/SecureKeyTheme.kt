package com.securekey.sdk.ui

import android.graphics.Color
import android.graphics.Typeface

/**
 * Theme configuration for the secure keyboard appearance.
 * Styled to match Gboard's Material Design aesthetic.
 */
data class SecureKeyTheme(
    val keyBackgroundColor: Int = Color.WHITE,
    val keyPressedColor: Int = Color.parseColor("#D4D6DA"),
    val keyTextColor: Int = Color.parseColor("#1B1B1F"),
    val keyboardBackgroundColor: Int = Color.parseColor("#ECEFF1"),
    val actionKeyColor: Int = Color.parseColor("#4285F4"),
    val actionKeyTextColor: Int = Color.WHITE,
    val modifierKeyColor: Int = Color.parseColor("#B8BCC2"),
    val modifierKeyTextColor: Int = Color.parseColor("#1B1B1F"),
    val modifierKeyPressedColor: Int = Color.parseColor("#9EA2A8"),
    val spaceBarColor: Int = Color.WHITE,
    val keyCornerRadius: Float = 16f,
    val keyElevation: Float = 1.5f,
    val keyTextSize: Float = 48f,
    val keyFontFamily: Typeface = Typeface.DEFAULT,
    val accentColor: Int = Color.parseColor("#4285F4"),
    val suggestionBarBackground: Int = Color.parseColor("#ECEFF1"),
    val popupBackgroundColor: Int = Color.parseColor("#616161"),
    val popupTextColor: Int = Color.WHITE,
    val rippleColor: Int = Color.parseColor("#404285F4"),
    val dividerColor: Int = Color.parseColor("#DADCE0"),
    val keyStrokeColor: Int = Color.TRANSPARENT
) {
    /** Apply this theme to a [KeyboardRenderer] */
    fun applyTo(renderer: KeyboardRenderer) {
        renderer.keyBackgroundColor = keyBackgroundColor
        renderer.keyPressedColor = keyPressedColor
        renderer.keyTextColor = keyTextColor
        renderer.keyboardBackgroundColor = keyboardBackgroundColor
        renderer.actionKeyColor = actionKeyColor
        renderer.actionKeyTextColor = actionKeyTextColor
        renderer.modifierKeyColor = modifierKeyColor
        renderer.modifierKeyTextColor = modifierKeyTextColor
        renderer.modifierKeyPressedColor = modifierKeyPressedColor
        renderer.spaceBarColor = spaceBarColor
        renderer.keyCornerRadius = keyCornerRadius
        renderer.keyElevation = keyElevation
        renderer.keyTextSize = keyTextSize
        renderer.keyStrokeColor = keyStrokeColor
    }
}
