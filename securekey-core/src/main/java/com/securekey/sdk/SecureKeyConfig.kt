package com.securekey.sdk

import android.graphics.Bitmap
import com.securekey.sdk.security.SecurityLevel
import com.securekey.sdk.ui.BrandingOverlay
import com.securekey.sdk.ui.SecureKeyTheme
import com.securekey.sdk.ui.ThemePresets
import java.util.Locale

/**
 * Configuration for the SecureKey SDK.
 * Use [Builder] for fluent construction.
 */
class SecureKeyConfig private constructor(
    val theme: SecureKeyTheme,
    val bankLogo: Bitmap?,
    val bankLogoPosition: BrandingOverlay.LogoPosition,
    val watermarkEnabled: Boolean,
    val watermarkOpacity: Float,
    val shuffleNumericKeys: Boolean,
    val hapticFeedbackEnabled: Boolean,
    val soundFeedbackEnabled: Boolean,
    val keyPreviewEnabled: Boolean,
    val securityLevel: SecurityLevel,
    val onSecurityThreat: OnSecurityThreatListener?,
    val doneButtonLabel: String,
    val allowPaste: Boolean,
    val allowScreenshot: Boolean,
    val otpAutoRead: Boolean,
    val amountSuggestions: List<Double>,
    val currency: Currency,
    val locale: Locale,
    val maxAmount: Double,
    val decimalPlaces: Int
) {
    class Builder {
        private var theme: SecureKeyTheme = ThemePresets.LIGHT
        private var bankLogo: Bitmap? = null
        private var bankLogoPosition: BrandingOverlay.LogoPosition = BrandingOverlay.LogoPosition.TOP_LEFT
        private var watermarkEnabled: Boolean = false
        private var watermarkOpacity: Float = 0.08f
        private var shuffleNumericKeys: Boolean = false
        private var hapticFeedbackEnabled: Boolean = true
        private var soundFeedbackEnabled: Boolean = false
        private var keyPreviewEnabled: Boolean = true
        private var securityLevel: SecurityLevel = SecurityLevel.STRICT
        private var onSecurityThreat: OnSecurityThreatListener? = null
        private var doneButtonLabel: String = "Done"
        private var allowPaste: Boolean = false
        private var allowScreenshot: Boolean = false
        private var otpAutoRead: Boolean = true
        private var amountSuggestions: List<Double> = listOf(100.0, 500.0, 1000.0, 5000.0)
        private var currency: Currency = Currency.USD
        private var locale: Locale = Locale.getDefault()
        private var maxAmount: Double = 999999999.99
        private var decimalPlaces: Int = 2

        fun theme(theme: SecureKeyTheme) = apply { this.theme = theme }
        fun bankLogo(logo: Bitmap?) = apply { this.bankLogo = logo }
        fun bankLogoPosition(position: BrandingOverlay.LogoPosition) = apply { this.bankLogoPosition = position }
        fun watermarkEnabled(enabled: Boolean) = apply { this.watermarkEnabled = enabled }
        fun watermarkOpacity(opacity: Float) = apply { this.watermarkOpacity = opacity }
        fun shuffleNumericKeys(shuffle: Boolean) = apply { this.shuffleNumericKeys = shuffle }
        fun hapticFeedback(enabled: Boolean) = apply { this.hapticFeedbackEnabled = enabled }
        fun soundFeedback(enabled: Boolean) = apply { this.soundFeedbackEnabled = enabled }
        fun keyPreview(enabled: Boolean) = apply { this.keyPreviewEnabled = enabled }
        fun securityLevel(level: SecurityLevel) = apply { this.securityLevel = level }
        fun onSecurityThreat(listener: OnSecurityThreatListener) = apply { this.onSecurityThreat = listener }
        fun doneButtonLabel(label: String) = apply { this.doneButtonLabel = label }
        fun allowPaste(allow: Boolean) = apply { this.allowPaste = allow }
        fun allowScreenshot(allow: Boolean) = apply { this.allowScreenshot = allow }
        fun otpAutoRead(enabled: Boolean) = apply { this.otpAutoRead = enabled }
        fun amountSuggestions(suggestions: List<Double>) = apply { this.amountSuggestions = suggestions }
        fun currency(currency: Currency) = apply { this.currency = currency }
        fun locale(locale: Locale) = apply { this.locale = locale }
        fun maxAmount(amount: Double) = apply { this.maxAmount = amount }
        fun decimalPlaces(places: Int) = apply { this.decimalPlaces = places }

        fun build() = SecureKeyConfig(
            theme, bankLogo, bankLogoPosition, watermarkEnabled, watermarkOpacity,
            shuffleNumericKeys, hapticFeedbackEnabled, soundFeedbackEnabled, keyPreviewEnabled,
            securityLevel, onSecurityThreat, doneButtonLabel, allowPaste, allowScreenshot,
            otpAutoRead, amountSuggestions, currency, locale, maxAmount, decimalPlaces
        )
    }
}
