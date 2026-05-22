package dev.strategia.aplino.security

import dev.strategia.aplino.TestBase
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
