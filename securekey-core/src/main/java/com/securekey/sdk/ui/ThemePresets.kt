package com.securekey.sdk.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

/**
 * Predefined theme presets matching Gboard styling.
 */
object ThemePresets {

    val LIGHT = SecureKeyTheme(
        keyBackgroundColor = Color.WHITE,
        keyPressedColor = Color.parseColor("#D4D6DA"),
        keyTextColor = Color.parseColor("#1B1B1F"),
        keyboardBackgroundColor = Color.parseColor("#ECEFF1"),
        actionKeyColor = Color.parseColor("#4285F4"),
        actionKeyTextColor = Color.WHITE,
        modifierKeyColor = Color.parseColor("#B8BCC2"),
        modifierKeyTextColor = Color.parseColor("#1B1B1F"),
        modifierKeyPressedColor = Color.parseColor("#9EA2A8"),
        spaceBarColor = Color.WHITE,
        popupBackgroundColor = Color.parseColor("#616161"),
        popupTextColor = Color.WHITE,
        keyStrokeColor = Color.TRANSPARENT
    )

    val DARK = SecureKeyTheme(
        keyBackgroundColor = Color.parseColor("#4A4A4E"),
        keyPressedColor = Color.parseColor("#5E5E62"),
        keyTextColor = Color.parseColor("#E8EAED"),
        keyboardBackgroundColor = Color.parseColor("#2C2C2E"),
        actionKeyColor = Color.parseColor("#8AB4F8"),
        actionKeyTextColor = Color.parseColor("#202124"),
        modifierKeyColor = Color.parseColor("#3C3C3F"),
        modifierKeyTextColor = Color.parseColor("#E8EAED"),
        modifierKeyPressedColor = Color.parseColor("#505054"),
        spaceBarColor = Color.parseColor("#4A4A4E"),
        popupBackgroundColor = Color.parseColor("#5E5E62"),
        popupTextColor = Color.WHITE,
        keyStrokeColor = Color.TRANSPARENT,
        dividerColor = Color.parseColor("#48484A"),
        suggestionBarBackground = Color.parseColor("#2C2C2E")
    )

    /** Returns LIGHT or DARK based on system configuration */
    fun system(context: Context): SecureKeyTheme {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) DARK else LIGHT
    }

    /** Create a custom theme by modifying a base preset */
    fun custom(base: SecureKeyTheme = LIGHT, builder: SecureKeyTheme.() -> SecureKeyTheme): SecureKeyTheme {
        return base.builder()
    }
}
