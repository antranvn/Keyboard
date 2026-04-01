package com.securekey.sdk.core

import android.graphics.RectF

/** Types of keyboard keys */
enum class KeyType {
    CHARACTER, NUMERIC, ACTION, MODIFIER, SPECIAL
}

/**
 * Represents a single key on the keyboard.
 */
data class Key(
    val bounds: RectF = RectF(),
    val label: String,
    val value: String = label,
    val type: KeyType = KeyType.CHARACTER,
    val widthWeight: Float = 1f
)
