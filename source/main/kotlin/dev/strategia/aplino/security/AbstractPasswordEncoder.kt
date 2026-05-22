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
        if (encodedPassword.length <= saltLength) {
            throw IllegalArgumentException("Unsalted password was provided.")
        }
        val decodedPass = Base64.getDecoder().decode(encodedPassword)
        val salt = decodedPass.sliceArray(0 until saltLength)
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
}
