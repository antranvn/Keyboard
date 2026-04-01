package com.securekey.sdk.core

/**
 * Full QWERTY keyboard layout with integrated number row, letter rows, and symbol layers.
 * Designed to match Gboard-style layout with 5 rows.
 */
internal object QwertyLayout {

    fun generate(doneLabel: String): KeyboardLayout {
        // Row 1: Number row
        val row1 = "1234567890".map {
            Key(label = it.toString(), value = it.toString(), type = KeyType.NUMERIC)
        }

        // Row 2: QWERTY top row
        val row2 = "qwertyuiop".map { Key(label = it.toString(), value = it.toString()) }

        // Row 3: Home row
        val row3 = "asdfghjkl".map { Key(label = it.toString(), value = it.toString()) }

        // Row 4: Bottom letter row with shift and backspace
        val row4Left = listOf(Key(label = "⇧", value = "SHIFT", type = KeyType.MODIFIER, widthWeight = 1.5f))
        val row4Letters = "zxcvbnm".map { Key(label = it.toString(), value = it.toString()) }
        val row4Right = listOf(Key(label = "⌫", value = "BACKSPACE", type = KeyType.MODIFIER, widthWeight = 1.5f))
        val row4 = row4Left + row4Letters + row4Right

        // Row 5: Bottom row with symbols toggle, comma, space, period, done
        val row5 = listOf(
            Key(label = "?123", value = "SYMBOLS", type = KeyType.MODIFIER, widthWeight = 1.5f),
            Key(label = ",", value = ","),
            Key(label = " ", value = " ", type = KeyType.SPECIAL, widthWeight = 5f),
            Key(label = ".", value = "."),
            Key(label = doneLabel, value = "DONE", type = KeyType.ACTION, widthWeight = 1.5f)
        )

        return KeyboardLayout(rows = listOf(row1, row2, row3, row4, row5), mode = KeyboardMode.QWERTY_FULL)
    }

    fun generateSymbols(layer: Int, doneLabel: String): KeyboardLayout {
        return if (layer == 0) {
            val row1 = "1234567890".map {
                Key(label = it.toString(), value = it.toString(), type = KeyType.NUMERIC)
            }
            val row2 = listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\"").map {
                Key(label = it, value = it)
            }
            val row3 = listOf(".", ",", "?", "!", "'", "#", "%", "^", "*").map {
                Key(label = it, value = it)
            }
            val row4Left = listOf(Key(label = "#+=", value = "SYMBOLS_2", type = KeyType.MODIFIER, widthWeight = 1.5f))
            val row4Symbols = listOf("+", "=", "_", "\\", "|", "~", "<", ">").map {
                Key(label = it, value = it)
            }
            val row4Right = listOf(Key(label = "⌫", value = "BACKSPACE", type = KeyType.MODIFIER, widthWeight = 1.5f))
            val row4 = row4Left + row4Symbols + row4Right

            val row5 = listOf(
                Key(label = "ABC", value = "LETTERS", type = KeyType.MODIFIER, widthWeight = 1.5f),
                Key(label = ",", value = ","),
                Key(label = " ", value = " ", type = KeyType.SPECIAL, widthWeight = 5f),
                Key(label = ".", value = "."),
                Key(label = doneLabel, value = "DONE", type = KeyType.ACTION, widthWeight = 1.5f)
            )

            KeyboardLayout(rows = listOf(row1, row2, row3, row4, row5), mode = KeyboardMode.QWERTY_FULL)
        } else {
            val row1 = "1234567890".map {
                Key(label = it.toString(), value = it.toString(), type = KeyType.NUMERIC)
            }
            val row2 = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "=").map {
                Key(label = it, value = it)
            }
            val row3 = listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥").map {
                Key(label = it, value = it)
            }
            val row4Left = listOf(Key(label = "123", value = "SYMBOLS", type = KeyType.MODIFIER, widthWeight = 1.5f))
            val row4Symbols = listOf("•", "©", "®", "™", "¿", "¡", "°", "…").map {
                Key(label = it, value = it)
            }
            val row4Right = listOf(Key(label = "⌫", value = "BACKSPACE", type = KeyType.MODIFIER, widthWeight = 1.5f))
            val row4 = row4Left + row4Symbols + row4Right

            val row5 = listOf(
                Key(label = "ABC", value = "LETTERS", type = KeyType.MODIFIER, widthWeight = 1.5f),
                Key(label = ",", value = ","),
                Key(label = " ", value = " ", type = KeyType.SPECIAL, widthWeight = 5f),
                Key(label = ".", value = "."),
                Key(label = doneLabel, value = "DONE", type = KeyType.ACTION, widthWeight = 1.5f)
            )

            KeyboardLayout(rows = listOf(row1, row2, row3, row4, row5), mode = KeyboardMode.QWERTY_FULL)
        }
    }
}
