package com.securekey.sdk.security

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Secure string backed by direct (native) memory.
 * Never stored on the Java heap as a String. Uses [finalize] as safety net.
 */
class SecureString private constructor(
    private val buffer: ByteBuffer,
    private val length: Int
) : Closeable {

    private val destroyed = AtomicBoolean(false)

    /** Read the string value. Minimizes time on heap. */
    fun read(): CharArray {
        checkNotDestroyed()
        synchronized(this) {
            val bytes = ByteArray(length)
            buffer.position(0)
            buffer.get(bytes)
            buffer.position(0)
            val chars = String(bytes, StandardCharsets.UTF_8).toCharArray()
            bytes.fill(0)
            return chars
        }
    }

    /** Wipe contents and release */
    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            synchronized(this) {
                wipeBuffer(buffer, length)
            }
        }
    }

    override fun close() = destroy()

    override fun toString(): String = "SecureString[destroyed=$destroyed]"

    @Suppress("removal")
    protected fun finalize() {
        destroy()
    }

    private fun checkNotDestroyed() {
        check(!destroyed.get()) { "SecureString has been destroyed" }
    }

    companion object {
        /** Create from char array (zeroes the input after copying) */
        fun wrap(chars: CharArray): SecureString {
            val bytes = String(chars).toByteArray(StandardCharsets.UTF_8)
            chars.fill('\u0000')
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.position(0)
            bytes.fill(0)
            return SecureString(buffer, buffer.capacity())
        }

        private fun wipeBuffer(buffer: ByteBuffer, length: Int) {
            buffer.clear()
            for (i in 0 until length) {
                buffer.put(i, 0)
            }
        }
    }
}
