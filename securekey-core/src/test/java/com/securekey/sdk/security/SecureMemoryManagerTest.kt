package com.securekey.sdk.security

import org.junit.Assert.*
import org.junit.Test

class SecureMemoryManagerTest {

    @Test
    fun `register and wipeAll destroys all buffers`() {
        val manager = SecureMemoryManager()
        val buf1 = manager.register(SecureBuffer.allocate(10))
        val buf2 = manager.register(SecureBuffer.allocate(10))
        buf1.put("test1".toByteArray())
        buf2.put("test2".toByteArray())

        manager.wipeAll()

        assertThrows(IllegalStateException::class.java) { buf1.get() }
        assertThrows(IllegalStateException::class.java) { buf2.get() }
    }

    @Test
    fun `onKeyboardDismiss wipes all`() {
        val manager = SecureMemoryManager()
        val buf = manager.register(SecureBuffer.allocate(10))
        buf.put("secret".toByteArray())

        manager.onKeyboardDismiss()

        assertThrows(IllegalStateException::class.java) { buf.get() }
    }

    @Test
    fun `onStop wipes all`() {
        val manager = SecureMemoryManager()
        val buf = manager.register(SecureBuffer.allocate(10))
        buf.put("secret".toByteArray())

        manager.onStop()

        assertThrows(IllegalStateException::class.java) { buf.get() }
    }
}
