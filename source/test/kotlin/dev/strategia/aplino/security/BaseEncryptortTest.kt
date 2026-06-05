package dev.strategia.aplino.security

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal class BaseEncryptortTest : TestBase() {
    private var encryptor = createEncryptor()

    @Test
    fun fastHash() {
        val content = "content"
        val hash = encryptor.hash(content.toByteArray())
        // May compare the result with: https://8gwifi.org/MessageDigest.jsp
        assertEquals("73a38b9e525c9c2ae262feeaa3c2947ab19bce3a173f075c75341e5e7fa080b6", hash)
    }

    @Test
    fun encryptData() {
        val data = "the secret data".toByteArray()
        val key = encryptor.createSecretKey("the password".toCharArray())
        val encryptedData = encryptor.encrypt(data, key)
        assertNotEquals(encryptedData, data)
        val decryptedData = encryptor.decrypt(encryptedData, key)
        assertArrayEquals(data, decryptedData)
    }

    @Test
    fun encryptionUsesRandomIv() {
        val data = "the secret data".toByteArray()
        val key = encryptor.createSecretKey("the password".toCharArray())
        val encrypted1 = encryptor.encrypt(data, key)
        val encrypted2 = encryptor.encrypt(data, key)
        // The same plaintext and key must produce different ciphertext (fresh IV each time).
        assertFalse(encrypted1.contentEquals(encrypted2))
        // Both ciphertexts must still decrypt back to the original data.
        assertArrayEquals(data, encryptor.decrypt(encrypted1, key))
        assertArrayEquals(data, encryptor.decrypt(encrypted2, key))
    }

    @Test
    fun encryptStreamRoundTrip() {
        val data = "the secret stream data".toByteArray()
        val key = encryptor.createSecretKey("the password".toCharArray())
        val encrypted = ByteArrayOutputStream()
        encryptor.encrypt(ByteArrayInputStream(data), encrypted, key)
        val decrypted = ByteArrayOutputStream()
        encryptor.decrypt(ByteArrayInputStream(encrypted.toByteArray()), decrypted, key)
        assertArrayEquals(data, decrypted.toByteArray())
    }

    private fun createEncryptor(): DataEncryptor {
        return BaseDataEncryptor()
    }
}
