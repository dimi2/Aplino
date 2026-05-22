package dev.strategia.aplino.security

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class AdvancedEncryptorTest: TestBase() {
    private var encryptor = createEncryptor()

    @Test
    fun fastHash() {
        //println("Available digest algorithms:")
        //val messageDigest: Set<String> = Security.getAlgorithms("MessageDigest")
        //messageDigest.forEach(Consumer { x: String? -> println(x) })

        val content = "content"
        val hash = encryptor.hash(content.toByteArray())
        // Can compare the result with: https://www.toolkitbay.com/tkb/tool/BLAKE3
        Assertions.assertEquals("3fba5250be9ac259c56e7250c526bc83bacb4be825f2799d3d59e5b4878dd74e", hash)
    }

    @Test
    fun encryptData() {
        val data = "the secret data".toByteArray()
        val key = encryptor.createSecretKey("the password".toCharArray())
        val encryptedData = encryptor.encrypt(data, key)
        Assertions.assertNotEquals(encryptedData, data)
        val decryptedData = encryptor.decrypt(encryptedData, key)
        Assertions.assertArrayEquals(data, decryptedData)
    }

    private fun createEncryptor(): DataEncryptor {
        return AdvancedDataEncryptor()
    }
}
