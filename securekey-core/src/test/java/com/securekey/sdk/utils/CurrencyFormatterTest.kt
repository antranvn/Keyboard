package com.securekey.sdk.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class CurrencyFormatterTest {

    @Test
    fun `format adds thousand separators`() {
        val formatter = CurrencyFormatter(locale = Locale.US)
        assertEquals("1,000", formatter.format("1000"))
    }

    @Test
    fun `format preserves decimals`() {
        val formatter = CurrencyFormatter(locale = Locale.US)
        assertEquals("1,234.56", formatter.format("1234.56"))
    }

    @Test
    fun `format limits decimal places`() {
        val formatter = CurrencyFormatter(locale = Locale.US, maxDecimalPlaces = 2)
        assertEquals("100.12", formatter.format("100.123"))
    }

    @Test
    fun `format with currency symbol`() {
        val formatter = CurrencyFormatter(locale = Locale.US, currencySymbol = "$")
        assertEquals("$ 1,000", formatter.format("1000"))
    }

    @Test
    fun `empty input returns empty`() {
        val formatter = CurrencyFormatter(locale = Locale.US)
        assertEquals("", formatter.format(""))
    }

    @Test
    fun `isWithinMax returns true for valid amount`() {
        val formatter = CurrencyFormatter(locale = Locale.US)
        assertTrue(formatter.isWithinMax("500", 1000.0))
    }

    @Test
    fun `isWithinMax returns false for exceeded amount`() {
        val formatter = CurrencyFormatter(locale = Locale.US)
        assertFalse(formatter.isWithinMax("1500", 1000.0))
    }
}
