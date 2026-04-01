package com.securekey.sdk.security

import java.io.Closeable
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Secure byte buffer backed by direct memory (off-heap).
 * Contents are zeroed on [destroy] or [close]. No toString() to prevent leakage.
 */
class SecureBuffer private constructor(
    private val buffer: ByteBuffer,
    private val capacity: Int
) : Closeable {

    private val destroyed = AtomicBoolean(false)

    /** Write data into the buffer at position 0 */
    fun put(data: ByteArray) {
        checkNotDestroyed()
        synchronized(this) {
            buffer.clear()
            buffer.put(data, 0, minOf(data.size, capacity))
            buffer.flip()
        }
    }

    /** Read data from the buffer */
    fun get(): ByteArray {
        checkNotDestroyed()
        synchronized(this) {
            val result = ByteArray(buffer.remaining())
            buffer.mark()
            buffer.get(result)
            buffer.reset()
            return result
        }
    }

    /** Append a single byte */
    fun append(b: Byte) {
        checkNotDestroyed()
        synchronized(this) {
            val pos = buffer.limit()
            if (pos < capacity) {
                buffer.limit(pos + 1)
                buffer.put(pos, b)
            }
        }
    }

    /** Remove the last byte (backspace) */
    fun removeLast() {
        checkNotDestroyed()
        synchronized(this) {
            val limit = buffer.limit()
            if (limit > 0) {
                buffer.put(limit - 1, 0)
                buffer.limit(limit - 1)
            }
        }
    }

    /** Current number of bytes stored */
    val size: Int
        get() {
            checkNotDestroyed()
            return buffer.remaining()
        }

    /** Zero all contents and mark as destroyed */
    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            synchronized(this) {
                buffer.clear()
                for (i in 0 until capacity) {
                    buffer.put(i, 0)
                }
                buffer.clear()
            }
        }
    }

    override fun close() = destroy()

    override fun toString(): String = "SecureBuffer[destroyed=$destroyed]"

    private fun checkNotDestroyed() {
        check(!destroyed.get()) { "SecureBuffer has been destroyed" }
    }

    companion object {
        /** Create a new SecureBuffer with the given capacity */
        fun allocate(capacity: Int): SecureBuffer {
            require(capacity > 0) { "Capacity must be positive" }
            val buffer = ByteBuffer.allocateDirect(capacity)
            buffer.limit(0)
            return SecureBuffer(buffer, capacity)
        }
    }
}
