package com.securekey.sdk.security

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SecureKeyDispatcherTest {

    private lateinit var dispatcher: SecureKeyDispatcher

    @Before
    fun setup() {
        dispatcher = SecureKeyDispatcher()
        dispatcher.initSession()
    }

    @After
    fun teardown() {
        dispatcher.destroySession()
    }

    @Test
    fun `encrypt and decrypt roundtrip`() {
        val original = "password123".toByteArray()
        val encrypted = dispatcher.encrypt(original)
        val decrypted = dispatcher.decrypt(encrypted)
        assertArrayEquals(original, decrypted)
    }

    @Test
    fun `different encryptions produce different ciphertexts`() {
        val data = "same-data".toByteArray()
        val encrypted1 = dispatcher.encrypt(data)
        val encrypted2 = dispatcher.encrypt(data)
        assertFalse(encrypted1.cipherText.contentEquals(encrypted2.cipherText))
    }

    @Test
    fun `different sessions produce different keys`() {
        val data = "test".toByteArray()
        val encrypted1 = dispatcher.encrypt(data)

        dispatcher.destroySession()
        dispatcher.initSession()

        val encrypted2 = dispatcher.encrypt(data)
        assertFalse(encrypted1.iv.contentEquals(encrypted2.iv) && encrypted1.cipherText.contentEquals(encrypted2.cipherText))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `encrypt without session throws`() {
        val fresh = SecureKeyDispatcher()
        fresh.encrypt("fail".toByteArray())
    }
}
