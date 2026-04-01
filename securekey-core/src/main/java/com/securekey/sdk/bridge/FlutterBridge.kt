package com.securekey.sdk.bridge

import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.security.IntegrityReport

/**
 * Bridge interface for Flutter integration.
 * MethodChannel: "com.securekey.sdk/keyboard"
 * Actual Flutter plugin implementation is in a separate package.
 */
abstract class FlutterBridge {

    /** Channel name for Flutter method calls */
    val channelName: String = "com.securekey.sdk/keyboard"

    abstract fun initialize(config: Map<String, Any>)

    abstract fun attachToInput(viewId: Int, mode: String)

    abstract fun attachOtpFields(viewIds: List<Int>, otpLength: Int)

    abstract fun attachAmountField(
        viewId: Int,
        currencyCode: String,
        suggestions: List<Double>,
        maxAmount: Double,
        decimalPlaces: Int
    )

    abstract fun show()

    abstract fun dismiss()

    abstract fun getSecurityReport(): Map<String, Any>

    /** Convert mode string from Dart to enum */
    protected fun parseMode(mode: String): KeyboardMode {
        return when (mode.uppercase()) {
            "QWERTY", "QWERTY_FULL" -> KeyboardMode.QWERTY_FULL
            "PIN", "NUMERIC_PIN" -> KeyboardMode.NUMERIC_PIN
            "OTP", "NUMERIC_OTP" -> KeyboardMode.NUMERIC_OTP
            "AMOUNT", "AMOUNT_PAD" -> KeyboardMode.AMOUNT_PAD
            else -> KeyboardMode.QWERTY_FULL
        }
    }

    /** Convert report to Dart-compatible map */
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
