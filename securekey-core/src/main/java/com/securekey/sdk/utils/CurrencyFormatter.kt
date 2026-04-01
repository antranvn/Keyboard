package com.securekey.sdk.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Live currency formatting as user types.
 * Locale-aware thousand separators with configurable decimal places.
 */
class CurrencyFormatter(
    private val locale: Locale = Locale.getDefault(),
    private val maxDecimalPlaces: Int = 2,
    private val currencySymbol: String = ""
) {
    private val symbols = DecimalFormatSymbols(locale)

    /** Format a raw numeric string into currency display format */
    fun format(rawInput: String): String {
        if (rawInput.isEmpty()) return ""

        val cleaned = rawInput.replace(symbols.groupingSeparator.toString(), "")
        val parts = cleaned.split(symbols.decimalSeparator)

        val integerPart = parts[0].ifEmpty { "0" }
        val decimalPart = if (parts.size > 1) {
            parts[1].take(maxDecimalPlaces)
        } else null

        // Format integer part with thousand separators
        val pattern = "#,###"
        val formatter = DecimalFormat(pattern, symbols)
        val formattedInteger = try {
            formatter.format(integerPart.toLong())
        } catch (_: NumberFormatException) {
            integerPart
        }

        val result = buildString {
            if (currencySymbol.isNotEmpty()) {
                append(currencySymbol)
                append(" ")
            }
            append(formattedInteger)
            if (decimalPart != null) {
                append(symbols.decimalSeparator)
                append(decimalPart)
            }
        }

        return result
    }

    /** Parse formatted string back to raw numeric value */
    fun parse(formattedInput: String): String {
        return formattedInput
            .replace(currencySymbol, "")
            .replace(symbols.groupingSeparator.toString(), "")
            .trim()
    }

    /** Validate that the amount doesn't exceed the maximum */
    fun isWithinMax(rawInput: String, maxAmount: Double): Boolean {
        return try {
            rawInput.replace(symbols.groupingSeparator.toString(), "").toDouble() <= maxAmount
        } catch (_: NumberFormatException) {
            true
        }
    }
}
