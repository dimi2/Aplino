package dev.strategia.aplino.security

/**
 * Holder for data encryption/decryption parameters.
 */
open class EncryptionParams : Cloneable {
    var algorithm: String? = null
    var cipherName: String? = null
    var seed: ByteArray? = null // Also called 'initialization vector'.
    var keyIterations: Int? = null
    var keyLength: Int? = null
    var keyOffset: Int? = null

    public override fun clone(): EncryptionParams {
        return super.clone() as EncryptionParams
    }
}
