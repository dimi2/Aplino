package dev.strategia.aplino.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.util.Base64

/**
 * Password encoder using [Argon2](https://en.wikipedia.org/wiki/Argon2) slow hash algorithm. With parameters
 * modification, it could adapt to faster hardware which will appear in the future.
 */
open class Argon2PasswordEncoder : AbstractPasswordEncoder {

    constructor(hashParams: HashParams) : super(hashParams)

    override fun encode(password: CharArray, salt: ByteArray?): String {
        var theSalt = salt
        if (theSalt == null) {
            theSalt = ByteArray(params.slowSaltLength!!)
            randomGenerator.nextBytes(theSalt)
        }
        val passwordHash = ByteArray(params.slowHashLength!!)
        val bytesGenerator = crateBytesGenerator(params, theSalt)
        bytesGenerator.generateBytes(password, passwordHash, 0, params.slowHashLength!!)
        val encoded = Base64.getEncoder().encodeToString(theSalt + passwordHash)
        return encoded
    }

    protected open fun crateBytesGenerator(params: HashParams, salt: ByteArray): Argon2BytesGenerator {
        val genParams = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13).withIterations(params.iterations!!)
            .withMemoryAsKB(params.memory!!).withParallelism(params.parallelism!!)
            .withSalt(salt)
        val generator = Argon2BytesGenerator()
        generator.init(genParams.build())
        return generator
    }

}
