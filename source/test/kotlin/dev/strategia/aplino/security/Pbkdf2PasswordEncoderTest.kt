package dev.strategia.aplino.security

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Base64
import javax.crypto.SecretKeyFactory

internal class Pbkdf2PasswordEncoderTest : TestBase() {

    @Test
    fun passwordMatch() {
        val params = HashParams()
        params.slowHashAlgorithm = "PBKDF2WithHmacSHA256"
        params.slowSaltLength = 1
        params.slowHashLength = 128
        params.iterations = 1
        val keyFactory = SecretKeyFactory.getInstance(params.slowHashAlgorithm!!)
        val encoder = Pbkdf2PasswordEncoder(params, keyFactory)

        val password = "rightPassword".toCharArray()
        val salt = ByteArray(params.slowSaltLength!!)
        salt.fill(8)
        val rightPasswordHash = encoder.encode(password, salt)
        val match = encoder.matches(password, rightPasswordHash)
        assertTrue(match)

        val wrongPassword = "wrongPassword".toByteArray()
        val wrongPasswordHash = Base64.getEncoder().encodeToString(wrongPassword)
        val noMatch = encoder.matches(password, wrongPasswordHash)
        assertFalse(noMatch)
    }

    @Test
    fun fixedSaltIsUsed() {
        val encoder = createEncoder(salt = ByteArray(16) { 7 }, seed = byteArrayOf(1, 2, 3))
        val hash1 = encoder.encode("secret".toCharArray(), null)
        val hash2 = encoder.encode("secret".toCharArray(), null)
        // A configured fixed salt makes the encoded output deterministic.
        assertEquals(hash1, hash2)
        // The password still verifies (the salt and seed are re-applied on verification).
        assertTrue(encoder.matches("secret".toCharArray(), hash1))
    }

    @Test
    fun seedActsAsPepper() {
        val withSeed = createEncoder(salt = null, seed = byteArrayOf(1, 2, 3))
        val hash = withSeed.encode("secret".toCharArray(), null)
        // The hash verifies when the same pepper is configured...
        assertTrue(withSeed.matches("secret".toCharArray(), hash))
        // ...but not when the pepper is missing.
        val withoutSeed = createEncoder(salt = null, seed = null)
        assertFalse(withoutSeed.matches("secret".toCharArray(), hash))
    }

    private fun createEncoder(salt: ByteArray?, seed: ByteArray?): Pbkdf2PasswordEncoder {
        val params = HashParams()
        params.slowHashAlgorithm = "PBKDF2WithHmacSHA256"
        params.slowSaltLength = 16
        params.slowHashLength = 16
        params.iterations = 1
        params.salt = salt
        params.seed = seed
        val keyFactory = SecretKeyFactory.getInstance(params.slowHashAlgorithm!!)
        return Pbkdf2PasswordEncoder(params, keyFactory)
    }

    @Test
    fun notEncodedPassword() {
        val params = HashParams()
        params.slowHashAlgorithm = "PBKDF2WithHmacSHA256"
        params.slowSaltLength = 32
        params.slowHashLength = 128
        params.iterations = 1
        val keyFactory = SecretKeyFactory.getInstance(params.slowHashAlgorithm!!)
        val encoder = Pbkdf2PasswordEncoder(params, keyFactory)

        val password = "password"
        try {
            encoder.matches(password.toCharArray(), password)
        } catch (_: IllegalArgumentException) {
            // This is expected.
        }
    }
}
