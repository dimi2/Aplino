package dev.strategia.aplino.security

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

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

    private fun createEncryptor(): DataEncryptor {
        return BaseDataEncryptor()
    }
}
