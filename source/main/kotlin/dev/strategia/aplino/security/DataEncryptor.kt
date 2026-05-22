package dev.strategia.aplino.security

import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.MessageDigest

/**
 * Interface for application data encryption/decryption.
 */
interface DataEncryptor {

    /**
     * Generate hash for provided input string. The hash is generated fast.
     * @param bytes Input string.
     * @param digester Digester to be used. Provide it if you do multiple hash calculations in a loop, in
     * a single thread (to reduce digester instances creation).
     * @return Generated hash as hex string.
     */
    fun hash(bytes: ByteArray, digester: MessageDigest? = null): String

    /**
     * Generate hash for provided input data. The hash is generated fast.
     * @param input Input data.
     * @param digester Digester to be used. Provide it if you do multiple hash calculations in a loop, in
     * a single thread (to reduce digester instances creation).
     * @return Generated hash as hex string.
     */
    fun hash(input: InputStream, digester: MessageDigest? = null): String

    /**
     * Generate hash for provided password. The hash generation is slow, and it is resistant to brute force
     * password recovery attacks.
     * @param password The password to be hashed.
     * @param encoder Password encoder to be used. Provide it if you do multiple hash calculations in a
     * loop, in a single thread (to reduce instances creation).
     * @return Generated hash as hex string.
     */
    fun encode(password: CharArray, encoder: PasswordEncoder? = null): String

    /**
     * Create secret key (derived from password) to be used for data encryption/decryption.
     * @param password The password.
     * @param withParams Encryption parameters. If not provided will use default.
     * @return Encryption key derived from the password.
     */
    fun createSecretKey(password: CharArray, withParams: EncryptionParams? = null): Key

    /**
     * Encrypt provided data.
     * @param data The data to be encrypted.
     * @param key Encryption key (derived from password).
     * @param withParams Encryption parameters. If not provided will use default.
     * @return The encrypted data.
     * @see decrypt
     */
    fun encrypt(data: ByteArray, key: Key, withParams: EncryptionParams? = null): ByteArray

    /**
     * Encrypt input data stream.
     * @param inputData Input data stream to be encrypted.
     * @param outputData Encrypted output data.
     * @param key Encryption key (derived from password).
     * @param withParams Encryption parameters. If not provided will use default.
     * @see decrypt
     */
    fun encrypt(inputData: InputStream, outputData: OutputStream, key: Key,
                withParams: EncryptionParams? = null)

    /**
     * Decrypt provided data.
     * @param data The data to be decrypted.
     * @param key Encryption key (derived from password). If not provided will use default.
     * @param withParams Encryption parameters. If not provided will use default.
     * @return The decrypted data.
     * @see encrypt
     */
    fun decrypt(data: ByteArray, key: Key, withParams: EncryptionParams? = null): ByteArray

    /**
     * Decrypt input data stream.
     * @param inputData Input data stream to be decrypted.
     * @param outputData Decrypted output data.
     * @param key Encryption key (derived from password).
     * @param withParams Encryption parameters. If not provided will use default.
     */
    fun decrypt(inputData: InputStream, outputData: OutputStream, key: Key,
                withParams: EncryptionParams? = null)

    /**
     * Create slow hash generator. It is intended for passwords hash generation.
     * @param params Hashing parameters.
     * @return Hash generator instance.
     * @see encode
     */
    fun createPasswordEncoder(params: HashParams): PasswordEncoder

    /**
     * Create fast hash generator. It is intended for checksums generation.
     * @param params Hashing parameters.
     * @return Hash generator instance.
     * @see hash
     */
    fun createHashGenerator(params: HashParams): MessageDigest

    /**
     * Get the default hash parameters of the service (used when custom params are not provided).
     * @return Hash parameters.
     */
    fun getHashParameters(): HashParams

    /**
     * Get the default encryption parameters of the service (used when custom params are not provided).
     * @return Encryption parameters.
     */
    fun getEncryptionParameters(): EncryptionParams

}
