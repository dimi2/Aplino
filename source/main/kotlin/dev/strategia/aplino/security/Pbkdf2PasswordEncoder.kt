package dev.strategia.aplino.security

import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password encoder using [PBKDF2](https://en.wikipedia.org/wiki/PBKDF2) slow hash algorithm.
 */
open class Pbkdf2PasswordEncoder : AbstractPasswordEncoder {
    protected var keyFactory: SecretKeyFactory

    constructor(hashParams: HashParams, keyFactory: SecretKeyFactory): super(hashParams) {
        this.keyFactory = keyFactory
    }

    override fun encode(password: CharArray, salt: ByteArray?): String {
        val theSalt = resolveSalt(salt)
        // slowHashLength is expressed in bytes (as for the Argon2 encoder); PBEKeySpec expects bits.
        @Suppress("MagicNumber")
        val keySpec = PBEKeySpec(password, derivationSalt(theSalt), params.iterations!!,
            params.slowHashLength!! * 8)
        // The default JDK secret key generation sporadically becomes very slow. This has something
        // common with the garbage collector. See:
        // https://bugs.openjdk.java.net/browse/JDK-8023983
        val secretKey = keyFactory.generateSecret(keySpec).encoded
        val key = SecretKeySpec(secretKey, params.slowHashAlgorithm)
        val encoded = Base64.getEncoder().encodeToString(theSalt + key.encoded)
        return encoded
    }

    /**
     * Build the salt fed into the actual key derivation. PBKDF2 has no dedicated pepper input, so the
     * optional [HashParams.seed] (pepper) is appended to the stored salt. Only the stored salt is kept
     * with the hash; the same seed must be configured again to verify the password later.
     * @param storedSalt The salt prepended to the encoded password.
     * @return The salt to use for the PBKDF2 derivation.
     */
    protected open fun derivationSalt(storedSalt: ByteArray): ByteArray {
        return params.seed?.let { storedSalt + it } ?: storedSalt
    }

}
