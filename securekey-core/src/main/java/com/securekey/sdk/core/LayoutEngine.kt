package com.securekey.sdk.core

/**
 * Factory that generates keyboard layouts for each mode.
 */
object LayoutEngine {

    /** Generate a layout for the given mode and configuration */
    fun generateLayout(
        mode: KeyboardMode,
        shuffleNumericKeys: Boolean = false,
        doneLabel: String = "Done",
        decimalSeparator: String = ".",
        currencySymbol: String = "$"
    ): KeyboardLayout {
        return when (mode) {
            KeyboardMode.QWERTY_FULL -> QwertyLayout.generate(doneLabel)
            KeyboardMode.NUMERIC_PIN -> NumericPinLayout.generate(shuffleNumericKeys, doneLabel)
            KeyboardMode.NUMERIC_OTP -> NumericOtpLayout.generate(shuffleNumericKeys, doneLabel)
            KeyboardMode.AMOUNT_PAD -> AmountPadLayout.generate(
                decimalSeparator, currencySymbol, doneLabel
            )
        }
    }

    /** Generate QWERTY symbol layers */
    fun generateSymbolLayout(layer: Int, doneLabel: String = "Done"): KeyboardLayout {
        return QwertyLayout.generateSymbols(layer, doneLabel)
    }
}
