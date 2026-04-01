package com.securekey.sdk.core

/**
 * Amount entry pad with decimal support and currency display.
 * Quick amount suggestions and formatting are handled at the InputProcessor level.
 */
internal object AmountPadLayout {

    fun generate(
        decimalSeparator: String,
        currencySymbol: String,
        doneLabel: String
    ): KeyboardLayout {
        val row1 = listOf(
            Key(label = "1", value = "1", type = KeyType.NUMERIC),
            Key(label = "2", value = "2", type = KeyType.NUMERIC),
            Key(label = "3", value = "3", type = KeyType.NUMERIC)
        )
        val row2 = listOf(
            Key(label = "4", value = "4", type = KeyType.NUMERIC),
            Key(label = "5", value = "5", type = KeyType.NUMERIC),
            Key(label = "6", value = "6", type = KeyType.NUMERIC)
        )
        val row3 = listOf(
            Key(label = "7", value = "7", type = KeyType.NUMERIC),
            Key(label = "8", value = "8", type = KeyType.NUMERIC),
            Key(label = "9", value = "9", type = KeyType.NUMERIC)
        )
        val row4 = listOf(
            Key(label = decimalSeparator, value = "DECIMAL", type = KeyType.SPECIAL),
            Key(label = "0", value = "0", type = KeyType.NUMERIC),
            Key(label = "⌫", value = "BACKSPACE", type = KeyType.ACTION)
        )
        val row5 = listOf(
            Key(label = doneLabel, value = "DONE", type = KeyType.ACTION, widthWeight = 1f)
        )

        return KeyboardLayout(
            rows = listOf(row1, row2, row3, row4, row5),
            mode = KeyboardMode.AMOUNT_PAD
        )
    }
}
