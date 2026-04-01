package com.securekey.sdk.core

import com.securekey.sdk.security.SecureBuffer

/**
 * SecureBuffer-backed current input with mode-specific validation.
 */
class InputBuffer(
    private val maxLength: Int = 512,
    private val mode: KeyboardMode = KeyboardMode.QWERTY_FULL
) {
    private var buffer = SecureBuffer.allocate(maxLength)
    private var length = 0

    /** Append a character, applying mode-specific validation */
    fun append(char: Char): Boolean {
        if (length >= maxLength) return false

        when (mode) {
            KeyboardMode.NUMERIC_PIN, KeyboardMode.NUMERIC_OTP -> {
                if (!char.isDigit()) return false
            }
            KeyboardMode.AMOUNT_PAD -> {
                if (!char.isDigit() && char != '.') return false
                val current = getCurrentText()
                if (char == '.' && current.contains('.')) return false
                val decimalIndex = current.indexOf('.')
                if (decimalIndex >= 0 && current.length - decimalIndex > 2) return false
            }
            else -> {}
        }

        buffer.append(char.code.toByte())
        length++
        return true
    }

    /** Remove the last character */
    fun removeLast(): Boolean {
        if (length <= 0) return false
        buffer.removeLast()
        length--
        return true
    }

    /** Get current text content */
    fun getCurrentText(): String {
        return String(buffer.get())
    }

    /** Get current length */
    fun getLength(): Int = length

    /** Check if buffer is full */
    fun isFull(): Boolean = length >= maxLength

    /** Reset buffer */
    fun reset() {
        buffer.destroy()
        buffer = SecureBuffer.allocate(maxLength)
        length = 0
    }

    /** Clean up */
    fun destroy() {
        buffer.destroy()
    }
}
