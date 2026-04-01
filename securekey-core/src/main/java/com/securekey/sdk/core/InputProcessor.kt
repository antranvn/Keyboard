package com.securekey.sdk.core

import com.securekey.sdk.security.SecureBuffer
import com.securekey.sdk.security.SecureKeyDispatcher

/**
 * Orchestrates touch -> key lookup -> modifier state -> encrypt -> dispatch.
 */
class InputProcessor(
    private val dispatcher: SecureKeyDispatcher
) {
    private val shiftState = ShiftState()
    private var inputBuffer = SecureBuffer.allocate(512)
    private var onCharacterInput: ((Char) -> Unit)? = null
    private var onActionKey: ((String) -> Unit)? = null
    private var onInputChanged: ((String) -> Unit)? = null

    /** Set listener for character input */
    fun setOnCharacterInput(listener: (Char) -> Unit) {
        onCharacterInput = listener
    }

    /** Set listener for action keys (DONE, BACKSPACE, etc.) */
    fun setOnActionKey(listener: (String) -> Unit) {
        onActionKey = listener
    }

    /** Set listener for input buffer changes */
    fun setOnInputChanged(listener: (String) -> Unit) {
        onInputChanged = listener
    }

    /** Process a key press */
    fun processKey(key: Key) {
        when (key.value) {
            "SHIFT" -> {
                shiftState.toggle()
                onActionKey?.invoke("SHIFT")
            }
            "BACKSPACE" -> {
                inputBuffer.removeLast()
                notifyInputChanged()
                onActionKey?.invoke("BACKSPACE")
            }
            "DONE" -> {
                onActionKey?.invoke("DONE")
            }
            "SYMBOLS", "SYMBOLS_2", "LETTERS" -> {
                onActionKey?.invoke(key.value)
            }
            "PASTE_OTP" -> {
                onActionKey?.invoke("PASTE_OTP")
            }
            "DECIMAL" -> {
                val char = '.'
                appendChar(char)
            }
            else -> {
                if (key.value.length == 1) {
                    var char = key.value[0]
                    if (shiftState.isActive && char.isLetter()) {
                        char = char.uppercaseChar()
                        shiftState.onCharacterTyped()
                    }
                    appendChar(char)
                }
            }
        }
    }

    private fun appendChar(char: Char) {
        val encrypted = dispatcher.encrypt(charArrayOf(char).toString().toByteArray())
        inputBuffer.append(char.code.toByte())
        onCharacterInput?.invoke(char)
        notifyInputChanged()
    }

    private fun notifyInputChanged() {
        val data = inputBuffer.get()
        onInputChanged?.invoke(String(data))
    }

    /** Get the current shift state */
    fun getShiftState(): ShiftState = shiftState

    /** Reset the input buffer */
    fun reset() {
        inputBuffer.destroy()
        inputBuffer = SecureBuffer.allocate(512)
        shiftState.reset()
    }

    /** Clean up resources */
    fun destroy() {
        inputBuffer.destroy()
    }
}
