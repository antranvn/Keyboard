package com.securekey.sdk.core

import android.graphics.RectF

/**
 * Represents the layout of a keyboard — a list of rows, each containing keys.
 */
data class KeyboardLayout(
    val rows: List<List<Key>>,
    val mode: KeyboardMode
) {
    /**
     * Calculate key bounds given the total available width and height.
     * [bottomPadding] reserves space at the bottom (e.g. for navigation bar).
     * Returns a new KeyboardLayout with bounds populated.
     */
    fun calculateBounds(
        width: Float,
        height: Float,
        keyGap: Float,
        bottomPadding: Float = 0f
    ): KeyboardLayout {
        if (rows.isEmpty()) return this

        val usableHeight = height - bottomPadding
        val rowHeight = (usableHeight - keyGap * (rows.size + 1)) / rows.size
        val newRows = rows.mapIndexed { rowIndex, row ->
            val totalWeight = row.sumOf { it.widthWeight.toDouble() }.toFloat()
            val availableWidth = width - keyGap * (row.size + 1)
            var x = keyGap

            row.map { key ->
                val keyWidth = (key.widthWeight / totalWeight) * availableWidth
                val y = keyGap + rowIndex * (rowHeight + keyGap)
                val newKey = key.copy(
                    bounds = RectF(x, y, x + keyWidth, y + rowHeight)
                )
                x += keyWidth + keyGap
                newKey
            }
        }
        return copy(rows = newRows)
    }

    /** Find the key at the given touch coordinates */
    fun findKeyAt(x: Float, y: Float): Key? {
        for (row in rows) {
            for (key in row) {
                if (key.bounds.contains(x, y)) return key
            }
        }
        return null
    }

    /**
     * Returns a new layout with letter key labels shifted to uppercase or lowercase.
     * Preserves all bounds and non-letter keys unchanged.
     */
    fun withShiftApplied(uppercase: Boolean): KeyboardLayout {
        val newRows = rows.map { row ->
            row.map { key ->
                if (key.type == KeyType.CHARACTER && key.label.length == 1 && key.label[0].isLetter()) {
                    val newLabel = if (uppercase) key.label.uppercase() else key.label.lowercase()
                    key.copy(label = newLabel)
                } else {
                    key
                }
            }
        }
        return copy(rows = newRows)
    }
}
