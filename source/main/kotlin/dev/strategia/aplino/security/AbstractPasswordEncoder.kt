package dev.strategia.aplino.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Base for implementing password encoders.
 */
abstract class AbstractPasswordEncoder : PasswordEncoder {
    protected var params: HashParams
    protected var randomGenerator: SecureRandom

    constructor(hashParams: HashParams) {
        params = hashParams
        randomGenerator = createRandomGenerator()
    }

    override fun matches(password: CharArray, encodedPassword: String) : Boolean {
        // Extract the salt, used initially to encode the password.
        val saltLength = params.slowSaltLength!!
        val decodedPassword = Base64.getDecoder().decode(encodedPassword)
        if (decodedPassword.size <= saltLength) {
            throw IllegalArgumentException("Unsalted password was provided.")
        }
        val salt = decodedPassword.sliceArray(0 until saltLength)
        // Encode the provided password.
        val pass = encode(password, salt)
        // Compare the passwords in constant time, to counter time measuring attacks.
        return MessageDigest.isEqual(pass.toByteArray(), encodedPassword.toByteArray())
    }

    protected open fun createRandomGenerator(): SecureRandom {
        // We do not use the secure `SecureRandom.getInstanceStrong()`, because it blocks waiting the native
        // random generator to accumulate enough entropy. See:
        // https://tersesystems.com/blog/2015/12/17/the-right-way-to-use-securerandom/
        // Actually both secure and unsecure instances get their random numbers from same place.
        return SecureRandom()
    }

    /**
     * Resolve the salt to store with the hash: the explicitly provided salt, the configured fixed
     * [HashParams.salt] (normalized to [HashParams.slowSaltLength] bytes), or a freshly generated random
     * salt of [HashParams.slowSaltLength] bytes.
     * @param salt Explicit salt, or null to use the configured or a random one.
     * @return The salt to prepend to the encoded password.
     */
    protected open fun resolveSalt(salt: ByteArray?): ByteArray {
        val saltLength = params.slowSaltLength!!
        val fixedSalt = params.salt
        val resolvedSalt: ByteArray
        if (salt != null) {
            resolvedSalt = salt
        } else if (fixedSalt != null) {
            resolvedSalt = fixedSalt.copyOf(saltLength)
        } else {
            resolvedSalt = ByteArray(saltLength)
            randomGenerator.nextBytes(resolvedSalt)
        }
        return resolvedSalt
    }
}
