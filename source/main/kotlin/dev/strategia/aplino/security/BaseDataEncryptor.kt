package dev.strategia.aplino.security

import dev.strategia.aplino.error.DataStructureException
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.MessageDigest
import java.security.SecureRandom
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
    companion object {
        /** GCM authentication tag length, in bits. */
        protected const val GCM_TAG_BITS = 128
        /** Default initialization vector length, in bytes (96 bits, recommended for GCM). */
        protected const val DEFAULT_IV_LENGTH = 12
    }

    protected var encryptionParams: EncryptionParams
    protected var hashParams: HashParams
    protected lateinit var hashGenerator: MessageDigest
    protected lateinit var passwordEncoder: PasswordEncoder
    protected lateinit var keyFactory: SecretKeyFactory
    protected var hashBufferSize = 1048576
    protected val secureRandom = SecureRandom()

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
        val keySpec = PBEKeySpec(password, params.keySalt, params.keyIterations!!, params.keyLength!!)
        return SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, params.algorithm)
    }

    override fun encrypt(data: ByteArray, key: Key, withParams: EncryptionParams?): ByteArray {
        val params = withParams ?: this.encryptionParams
        // Use a fresh random initialization vector for each encryption and prepend it to the result.
        val iv = generateIv(params)
        val cipher: Cipher = createCipher(key, Cipher.ENCRYPT_MODE, params, iv)
        val encrypted: ByteArray
        try {
            encrypted = cipher.doFinal(data)
        } catch (e: Exception) {
            throw DataStructureException("Data encryption error.", e)
        }
        return iv + encrypted
    }

    override fun encrypt(inputData: InputStream, outputData: OutputStream, key: Key,
                withParams: EncryptionParams?) {
        val params = withParams ?: encryptionParams
        val iv = generateIv(params)
        // Prepend the initialization vector to the output, so it can be recovered on decryption.
        outputData.write(iv)
        val cipher = createCipher(key, Cipher.ENCRYPT_MODE, params, iv)
        val input = CipherInputStream(inputData, cipher)
        input.copyTo(outputData)
    }

    override fun decrypt(data: ByteArray, key: Key, withParams: EncryptionParams?): ByteArray {
        val params = withParams ?: this.encryptionParams
        val ivLength = params.ivLength ?: DEFAULT_IV_LENGTH
        if (data.size < ivLength) {
            throw DataStructureException("Encrypted data is too short to contain the initialization vector.")
        }
        // The initialization vector is prepended to the encrypted data.
        val iv = data.copyOfRange(0, ivLength)
        val cipherText = data.copyOfRange(ivLength, data.size)
        val cipher: Cipher = createCipher(key, Cipher.DECRYPT_MODE, params, iv)
        val decrypted: ByteArray
        try {
            decrypted = cipher.doFinal(cipherText)
        } catch (e: Exception) {
            throw DataStructureException("Data decryption error.", e)
        }
        return decrypted
    }

    override fun decrypt(inputData: InputStream, outputData: OutputStream, key: Key,
                         withParams: EncryptionParams?) {
        val params = withParams ?: encryptionParams
        val ivLength = params.ivLength ?: DEFAULT_IV_LENGTH
        // The initialization vector is prepended to the encrypted stream.
        val iv = inputData.readNBytes(ivLength)
        if (iv.size < ivLength) {
            throw DataStructureException(
                "Encrypted stream is too short to contain the initialization vector.")
        }
        val cipher = createCipher(key, Cipher.DECRYPT_MODE, params, iv)
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
        // Fixed default salt for deterministic key derivation. Deployments should override it
        // with their own value through custom EncryptionParams.
        params.keySalt = byteArrayOf(
            0x5A, 0x1F, 0x73, 0x29.toByte(), 0xC4.toByte(), 0x08, 0x6B, 0xE2.toByte(),
            0x4D, 0x90.toByte(), 0x17, 0xA8.toByte(), 0x3C, 0xD1.toByte(), 0x62, 0x0E)
        params.ivLength = DEFAULT_IV_LENGTH
        params.keyIterations = 1000
        params.keyLength = 256
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

    /**
     * Generate a fresh random initialization vector for an encryption operation.
     * @param params Encryption parameters (determine the IV length).
     * @return The generated initialization vector.
     */
    protected open fun generateIv(params: EncryptionParams): ByteArray {
        val iv = ByteArray(params.ivLength ?: DEFAULT_IV_LENGTH)
        secureRandom.nextBytes(iv)
        return iv
    }

    protected open fun createCipher(key: Key, mode: Int, params: EncryptionParams, iv: ByteArray): Cipher {
        val cipher: Cipher = Cipher.getInstance(params.cipherName)
        try {
            cipher.init(mode, key, GCMParameterSpec(GCM_TAG_BITS, iv))
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
