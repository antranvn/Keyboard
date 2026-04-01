package com.securekey.sdk.bridge

import com.securekey.sdk.Currency
import com.securekey.sdk.SecureKey
import com.securekey.sdk.SecureKeyConfig
import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.security.IntegrityReport
import com.securekey.sdk.security.ThreatType

/**
 * Bridge interface for React Native integration.
 * Matches ReactContextBaseJavaModule pattern.
 * Actual RN module implementation is in a separate package.
 */
abstract class ReactNativeBridge {

    abstract fun initialize(config: Map<String, Any>)

    abstract fun attachToInput(reactTag: Int, mode: String)

    abstract fun attachOtpFields(reactTags: List<Int>, otpLength: Int)

    abstract fun attachAmountField(
        reactTag: Int,
        currencyCode: String,
        suggestions: List<Double>,
        maxAmount: Double,
        decimalPlaces: Int
    )

    abstract fun show()

    abstract fun dismiss()

    abstract fun getSecurityReport(callback: (Map<String, Any>) -> Unit)

    /** Convert keyboard mode string to enum */
    protected fun parseMode(mode: String): KeyboardMode {
        return when (mode.uppercase()) {
            "QWERTY", "QWERTY_FULL" -> KeyboardMode.QWERTY_FULL
            "PIN", "NUMERIC_PIN" -> KeyboardMode.NUMERIC_PIN
            "OTP", "NUMERIC_OTP" -> KeyboardMode.NUMERIC_OTP
            "AMOUNT", "AMOUNT_PAD" -> KeyboardMode.AMOUNT_PAD
            else -> KeyboardMode.QWERTY_FULL
        }
    }

    /** Convert IntegrityReport to a map for JS bridge */
    protected fun reportToMap(report: IntegrityReport): Map<String, Any> {
        return mapOf(
            "isSecure" to report.isSecure,
            "activeKeyloggers" to report.activeKeyloggers,
            "hasScreenOverlay" to report.hasScreenOverlay,
            "isScreenBeingRecorded" to report.isScreenBeingRecorded,
            "threats" to report.detectedThreats.map { it.name }
        )
    }
}
