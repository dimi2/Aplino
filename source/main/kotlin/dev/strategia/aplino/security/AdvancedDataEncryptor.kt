package dev.strategia.aplino.security

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Key
import java.security.MessageDigest
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec

/**
 * Advanced security service implementation.
 * It depends on [BouncyCastle](https://www.bouncycastle.org/download/bouncy-castle-java/) encryption library,
 * which supports more algorithms than default JDK crypto provider.
 */
open class AdvancedDataEncryptor : BaseDataEncryptor {

    constructor() : super()

    override fun createPasswordEncoder(params: HashParams): PasswordEncoder {
        return Argon2PasswordEncoder(params)
    }

    override fun createCipher(key: Key, mode: Int, params: EncryptionParams): Cipher {
        val cipher: Cipher = Cipher.getInstance(params.cipherName)
        try {
            cipher.init(mode, key, IvParameterSpec(params.seed))
        } catch (e: Exception) {
            throw RuntimeException("Cipher creation error.", e)
        }
        return cipher
    }

    override fun createKeyFactory(algorithmName: String): SecretKeyFactory {
        return SecretKeyFactory.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME)
    }

    override fun createHashGenerator(params: HashParams): MessageDigest {
        return MessageDigest.getInstance(params.fastHashAlgorithm, BouncyCastleProvider.PROVIDER_NAME)
    }

    @Suppress("MagicNumber")
    override fun defaultHashParams(): HashParams {
        val params = HashParams()
        params.fastHashAlgorithm = "Blake3-256"
        params.slowHashAlgorithm = "Argon2id"
        params.keyAlgorithm = "PBKDF2WithHmacSHA3-256"
        params.slowHashLength = 384
        params.slowSaltLength = (512 - 384)
        params.iterations = 45000
        params.memory = 32
        params.parallelism = 4
        params.seed = byteArrayOf(0x44) // D
        params.salt = null
        return params
    }

    override fun initInternal() {
        Security.addProvider(BouncyCastleProvider())
        super.initInternal()
    }
}
