package com.securekey.sdk.security

import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM encrypted keystroke dispatch.
 * Per-session ephemeral key via ECDH with HKDF-SHA256 key derivation.
 */
class SecureKeyDispatcher {

    private var sessionKey: ByteArray? = null

    /** Initialize a new session with ephemeral ECDH key exchange */
    fun initSession() {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())
        val senderPair = kpg.generateKeyPair()
        val receiverPair = kpg.generateKeyPair()

        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(senderPair.private)
        ka.doPhase(receiverPair.public, true)
        val sharedSecret = ka.generateSecret()

        sessionKey = hkdfSha256(sharedSecret, "SecureKey-Session".toByteArray(), 32)
        sharedSecret.fill(0)
    }

    /** Encrypt a keystroke value */
    fun encrypt(data: ByteArray): EncryptedPayload {
        val key = requireNotNull(sessionKey) { "Session not initialized" }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(data)
        return EncryptedPayload(cipherText, iv)
    }

    /** Decrypt a keystroke payload */
    fun decrypt(payload: EncryptedPayload): ByteArray {
        val key = requireNotNull(sessionKey) { "Session not initialized" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, payload.iv))
        return cipher.doFinal(payload.cipherText)
    }

    /** Destroy session key material */
    fun destroySession() {
        sessionKey?.fill(0)
        sessionKey = null
    }

    /** HKDF-SHA256 extract-and-expand */
    private fun hkdfSha256(ikm: ByteArray, info: ByteArray, length: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        // Extract
        val salt = ByteArray(32) // zero salt
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = mac.doFinal(ikm)

        // Expand
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        val result = ByteArray(length)
        var t = ByteArray(0)
        var offset = 0
        var counter: Byte = 1
        while (offset < length) {
            mac.update(t)
            mac.update(info)
            mac.update(counter)
            t = mac.doFinal()
            val toCopy = minOf(t.size, length - offset)
            System.arraycopy(t, 0, result, offset, toCopy)
            offset += toCopy
            counter++
        }
        prk.fill(0)
        return result
    }

    /** Encrypted payload containing ciphertext and IV */
    data class EncryptedPayload(val cipherText: ByteArray, val iv: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EncryptedPayload) return false
            return cipherText.contentEquals(other.cipherText) && iv.contentEquals(other.iv)
        }
        override fun hashCode(): Int = 31 * cipherText.contentHashCode() + iv.contentHashCode()
    }
}
