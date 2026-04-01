package com.securekey.sdk.core

/**
 * 3x4 numeric PIN pad layout with optional key randomization.
 */
internal object NumericPinLayout {

    fun generate(shuffle: Boolean, doneLabel: String): KeyboardLayout {
        val digits = if (shuffle) {
            (0..9).toMutableList().also { it.shuffle() }
        } else {
            (0..9).toList()
        }

        val row1 = (0..2).map { i ->
            Key(label = digits[i + 1].toString(), value = digits[i + 1].toString(), type = KeyType.NUMERIC)
        }
        val row2 = (0..2).map { i ->
            Key(label = digits[i + 4].toString(), value = digits[i + 4].toString(), type = KeyType.NUMERIC)
        }
        val row3 = (0..2).map { i ->
            Key(label = digits[i + 7].toString(), value = digits[i + 7].toString(), type = KeyType.NUMERIC)
        }
        val row4 = listOf(
            Key(label = doneLabel, value = "DONE", type = KeyType.ACTION),
            Key(label = digits[0].toString(), value = digits[0].toString(), type = KeyType.NUMERIC),
            Key(label = "⌫", value = "BACKSPACE", type = KeyType.ACTION)
        )

        return KeyboardLayout(rows = listOf(row1, row2, row3, row4), mode = KeyboardMode.NUMERIC_PIN)
    }
}
