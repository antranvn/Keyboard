package com.securekey.sdk.security

import java.util.Collections

/**
 * Manages all [SecureBuffer] and [SecureString] instances.
 * Wipes everything on keyboard dismiss or activity stop.
 */
class SecureMemoryManager {

    private val buffers = Collections.synchronizedList(mutableListOf<SecureBuffer>())
    private val strings = Collections.synchronizedList(mutableListOf<SecureString>())

    /** Register a buffer for lifecycle management */
    fun register(buffer: SecureBuffer): SecureBuffer {
        buffers.add(buffer)
        return buffer
    }

    /** Register a string for lifecycle management */
    fun register(string: SecureString): SecureString {
        strings.add(string)
        return string
    }

    /** Wipe all managed buffers and strings */
    fun wipeAll() {
        synchronized(buffers) {
            buffers.forEach { it.destroy() }
            buffers.clear()
        }
        synchronized(strings) {
            strings.forEach { it.destroy() }
            strings.clear()
        }
    }

    /** Called when keyboard is dismissed */
    fun onKeyboardDismiss() = wipeAll()

    /** Called on Activity onStop */
    fun onStop() = wipeAll()
}
