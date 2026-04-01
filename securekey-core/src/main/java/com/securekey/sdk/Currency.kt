package com.securekey.sdk

/**
 * Supported currencies for amount pad.
 */
@JvmInline
value class Currency(val code: String) {
    val symbol: String
        get() = when (code) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "INR" -> "₹"
            "CNY" -> "¥"
            "KRW" -> "₩"
            "BRL" -> "R$"
            "AUD" -> "A$"
            "CAD" -> "C$"
            else -> code
        }

    companion object {
        val USD = Currency("USD")
        val EUR = Currency("EUR")
        val GBP = Currency("GBP")
        val JPY = Currency("JPY")
        val INR = Currency("INR")
    }
}
