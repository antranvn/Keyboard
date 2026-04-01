package com.securekey.sdk.core

import org.junit.Assert.*
import org.junit.Test

class KeyboardLayoutTest {

    @Test
    fun `QWERTY layout generates all rows`() {
        val layout = LayoutEngine.generateLayout(KeyboardMode.QWERTY_FULL)
        assertEquals(5, layout.rows.size)
        assertEquals(KeyboardMode.QWERTY_FULL, layout.mode)
    }

    @Test
    fun `QWERTY first row has 10 keys`() {
        val layout = LayoutEngine.generateLayout(KeyboardMode.QWERTY_FULL)
        assertEquals(10, layout.rows[0].size)
    }

    @Test
    fun `numeric PIN layout has 4 rows`() {
        val layout = LayoutEngine.generateLayout(KeyboardMode.NUMERIC_PIN)
        assertEquals(4, layout.rows.size)
        assertEquals(KeyboardMode.NUMERIC_PIN, layout.mode)
    }

    @Test
    fun `numeric PIN has 3 keys per row`() {
        val layout = LayoutEngine.generateLayout(KeyboardMode.NUMERIC_PIN)
        layout.rows.forEach { row ->
            assertEquals(3, row.size)
        }
    }

    @Test
    fun `numeric PIN shuffle produces different order`() {
        val layouts = (1..10).map {
            LayoutEngine.generateLayout(KeyboardMode.NUMERIC_PIN, shuffleNumericKeys = true)
        }
        val orders = layouts.map { layout ->
            layout.rows.flatMap { row -> row.map { it.label } }
        }.toSet()
        // With 10 attempts, shuffled layout should produce at least 2 different orderings
        assertTrue("Shuffle should produce different orders", orders.size > 1)
    }

    @Test
    fun `amount pad layout has 5 rows`() {
        val layout = LayoutEngine.generateLayout(KeyboardMode.AMOUNT_PAD)
        assertEquals(5, layout.rows.size)
        assertEquals(KeyboardMode.AMOUNT_PAD, layout.mode)
    }

    @Test
    fun `OTP layout has paste button`() {
        val layout = LayoutEngine.generateLayout(KeyboardMode.NUMERIC_OTP)
        val allKeys = layout.rows.flatten()
        assertTrue(allKeys.any { it.value == "PASTE_OTP" })
    }

    @Test
    fun `symbol layout layer 0 has numbers`() {
        val layout = LayoutEngine.generateSymbolLayout(0)
        val firstRow = layout.rows[0]
        assertEquals(10, firstRow.size)
        assertTrue(firstRow.all { it.type == KeyType.NUMERIC || it.type == KeyType.CHARACTER })
    }

    @Test
    fun `custom done label is used`() {
        val layout = LayoutEngine.generateLayout(
            KeyboardMode.QWERTY_FULL,
            doneLabel = "Submit"
        )
        val allKeys = layout.rows.flatten()
        assertTrue(allKeys.any { it.label == "Submit" && it.value == "DONE" })
    }

    // calculateBounds and findKeyAt tests require Android instrumented tests
    // since they depend on android.graphics.RectF
}
