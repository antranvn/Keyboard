package com.securekey.sdk.security

import org.junit.Assert.*
import org.junit.Test

class SecureBufferTest {

    @Test
    fun `put and get roundtrip`() {
        val buffer = SecureBuffer.allocate(256)
        val data = "Hello, SecureKey!".toByteArray()
        buffer.put(data)
        assertArrayEquals(data, buffer.get())
        buffer.close()
    }

    @Test
    fun `append adds bytes`() {
        val buffer = SecureBuffer.allocate(10)
        buffer.append('A'.code.toByte())
        buffer.append('B'.code.toByte())
        assertEquals(2, buffer.size)
        assertEquals("AB", String(buffer.get()))
        buffer.close()
    }

    @Test
    fun `removeLast removes last byte`() {
        val buffer = SecureBuffer.allocate(10)
        buffer.append('A'.code.toByte())
        buffer.append('B'.code.toByte())
        buffer.removeLast()
        assertEquals(1, buffer.size)
        assertEquals("A", String(buffer.get()))
        buffer.close()
    }

    @Test
    fun `destroy zeroes memory`() {
        val buffer = SecureBuffer.allocate(10)
        buffer.put("secret".toByteArray())
        buffer.destroy()
        assertThrows(IllegalStateException::class.java) {
            buffer.get()
        }
    }

    @Test
    fun `toString does not leak content`() {
        val buffer = SecureBuffer.allocate(10)
        buffer.put("secret".toByteArray())
        assertFalse(buffer.toString().contains("secret"))
        buffer.close()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `allocate with zero capacity throws`() {
        SecureBuffer.allocate(0)
    }

    @Test(expected = IllegalStateException::class)
    fun `operations after destroy throw`() {
        val buffer = SecureBuffer.allocate(10)
        buffer.destroy()
        buffer.put("fail".toByteArray())
    }
}
