package dev.strategia.aplino.security

/**
 * Holder for data encryption/decryption parameters.
 */
open class EncryptionParams : Cloneable {
    var algorithm: String? = null
    var cipherName: String? = null
    /** Salt for the password-based key derivation. Should be overridden per deployment. */
    var keySalt: ByteArray? = null
    /** Length (in bytes) of the random initialization vector generated for each encryption. */
    var ivLength: Int? = null
    var keyIterations: Int? = null
    var keyLength: Int? = null

    public override fun clone(): EncryptionParams {
        val clone = super.clone() as EncryptionParams
        // Deep copy the mutable arrays, so callers cannot mutate the original through the clone.
        clone.keySalt = keySalt?.copyOf()
        return clone
    }
}
