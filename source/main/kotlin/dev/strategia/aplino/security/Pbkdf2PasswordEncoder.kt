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
        var theSalt = salt
        if (theSalt == null) {
            theSalt = ByteArray(params.slowSaltLength!!)
            randomGenerator.nextBytes(theSalt)
        }
        val keySpec = PBEKeySpec(password, theSalt, params.iterations!!, params.slowHashLength!!)
        // The default JDK secret key generation sporadically becomes very slow. This has something
        // common with the garbage collector. See:
        // https://bugs.openjdk.java.net/browse/JDK-8023983
        val secretKey = keyFactory.generateSecret(keySpec).encoded
        val key = SecretKeySpec(secretKey, params.slowHashAlgorithm)
        val encoded = Base64.getEncoder().encodeToString(theSalt + key.encoded)
        return encoded
    }

}
