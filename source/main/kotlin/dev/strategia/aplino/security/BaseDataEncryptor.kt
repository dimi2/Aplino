package dev.strategia.aplino.security

import dev.strategia.aplino.error.DataStructureException
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.MessageDigest
import java.util.HexFormat
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Base data encryptor for the application.
 */
open class BaseDataEncryptor : DataEncryptor {
    protected var encryptionParams: EncryptionParams
    protected var hashParams: HashParams
    protected lateinit var hashGenerator: MessageDigest
    protected lateinit var passwordEncoder: PasswordEncoder
    protected lateinit var keyFactory: SecretKeyFactory
    protected var hashBufferSize = 1048576

    constructor(encryptionParams: EncryptionParams? = null, params: HashParams? = null) {
        this.encryptionParams = encryptionParams ?: defaultEncryptionParams()
        this.hashParams = params ?: defaultHashParams()
        initInternal()
    }

    override fun hash(bytes: ByteArray, digester: MessageDigest?): String {
        val theDigester = digester ?: (hashGenerator.clone() as MessageDigest)
        return HexFormat.of().formatHex(theDigester.digest(bytes))
    }

    override fun hash(input: InputStream, digester: MessageDigest?): String {
        val theDigester = digester ?: (hashGenerator.clone() as MessageDigest)
        val bufLength = hashBufferSize
        val buf = ByteArray(bufLength)
        var r: Int
        do {
            r = input.read(buf, 0, bufLength)
            if (r > 0) {
                theDigester.update(buf, 0, r)
            }
        } while (r != -1) //
        return HexFormat.of().formatHex(theDigester.digest())
    }

    override fun encode(password: CharArray, encoder: PasswordEncoder?): String {
        val theEncoder = encoder ?: passwordEncoder
        val passwordHash = theEncoder.encode(password)
        return passwordHash
    }

    override fun createSecretKey(password: CharArray, withParams: EncryptionParams?): Key {
        val params = withParams ?: encryptionParams
        val keySpec = PBEKeySpec(password, params.seed, params.keyIterations!!, params.keyLength!!)
        return SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, params.algorithm)
    }

    override fun encrypt(data: ByteArray, key: Key, withParams: EncryptionParams?): ByteArray {
        val cipher: Cipher = createCipher(key, Cipher.ENCRYPT_MODE, withParams ?: this.encryptionParams)
        val encrypted: ByteArray
        try {
            encrypted = cipher.doFinal(data)
        } catch (e: Exception) {
            throw DataStructureException("Data encryption error.", e)
        }
        return encrypted
    }

    override fun encrypt(inputData: InputStream, outputData: OutputStream, key: Key,
                withParams: EncryptionParams?) {
        val cipher = createCipher(key, Cipher.ENCRYPT_MODE, withParams ?: encryptionParams)
        val input = CipherInputStream(inputData, cipher)
        input.copyTo(outputData)
    }

    override fun decrypt(data: ByteArray, key: Key, withParams: EncryptionParams?): ByteArray {
        val cipher: Cipher = createCipher(key, Cipher.DECRYPT_MODE, withParams ?: this.encryptionParams)
        val decrypted: ByteArray
        try {
            decrypted = cipher.doFinal(data)
        } catch (e: Exception) {
            throw DataStructureException("Data decryption error.", e)
        }
        return decrypted
    }

    override fun decrypt(inputData: InputStream, outputData: OutputStream, key: Key,
                         withParams: EncryptionParams?) {
        val cipher = createCipher(key, Cipher.DECRYPT_MODE, withParams ?: encryptionParams)
        val input = CipherInputStream(inputData, cipher)
        input.copyTo(outputData)
    }

    override fun getHashParameters(): HashParams {
        return hashParams.clone()
    }

    override fun getEncryptionParameters(): EncryptionParams {
        return encryptionParams.clone()
    }

    override fun createPasswordEncoder(params: HashParams): PasswordEncoder {
        val kf = createKeyFactory(hashParams.slowHashAlgorithm!!)
        return Pbkdf2PasswordEncoder(params, kf)
    }

    override fun createHashGenerator(params: HashParams): MessageDigest {
        return MessageDigest.getInstance(params.fastHashAlgorithm)
    }

    @Suppress("MagicNumber")
    protected open fun defaultEncryptionParams(): EncryptionParams {
        val params = EncryptionParams()
        params.algorithm = "AES"
        params.cipherName = "AES/GCM/NoPadding"
        params.seed = byteArrayOf(0x5A) // Z
        params.keyIterations = 1000
        params.keyLength = 256
        params.keyOffset = 7
        return params
    }

    @Suppress("MagicNumber")
    protected open fun defaultHashParams(): HashParams {
        val params = HashParams()
        params.fastHashAlgorithm = "SHA3-256"
        params.slowHashAlgorithm = "PBKDF2WithHmacSHA256"
        params.keyAlgorithm = "PBKDF2WithHmacSHA256"
        params.slowHashLength = 384
        params.slowSaltLength = (512 - 384)
        params.iterations = 100000
        params.memory = 32
        params.parallelism = 4
        params.seed = byteArrayOf(0x44) // D
        params.salt = null
        return params
    }

    @Suppress("MagicNumber")
    protected open fun createCipher(key: Key, mode: Int, params: EncryptionParams): Cipher {
        val cipher: Cipher = Cipher.getInstance(params.cipherName)
        try {
            cipher.init(mode, key, GCMParameterSpec(128, params.seed))
        } catch (e: Exception) {
            throw RuntimeException("Cipher creation error.", e)
        }
        return cipher
    }

    protected open fun createKeyFactory(algorithmName: String): SecretKeyFactory {
        return SecretKeyFactory.getInstance(algorithmName)
    }

    protected open fun initInternal() {
        keyFactory = createKeyFactory(hashParams.keyAlgorithm!!)
        passwordEncoder = createPasswordEncoder(hashParams)
        hashGenerator = createHashGenerator(hashParams)
    }
}
