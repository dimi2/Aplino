package dev.strategia.aplino.security

/**
 * Holder for hash generation parameters (in one place).
 */
open class HashParams: Cloneable {
    /** Algorithm name for the fast (general purpose) hash, e.g. "SHA3-256" or "Blake3-256". */
    var fastHashAlgorithm: String? = null
    /** Algorithm name for the slow (password) hash, e.g. "PBKDF2WithHmacSHA256" or "Argon2id". */
    var slowHashAlgorithm: String? = null
    /** Algorithm name for the secret key factory used in key derivation, e.g. "PBKDF2WithHmacSHA256". */
    var keyAlgorithm: String? = null
    /** Length of the slow hash output, in bytes. */
    var slowHashLength: Int? = null
    /** Length of the salt used by the slow hash, in bytes. */
    var slowSaltLength: Int? = null
    /**
     * Optional fixed salt for the slow hash. When null, a random salt is generated per password.
     * Note: a fixed salt weakens password hashing (identical passwords yield identical hashes), so prefer
     * the random default; it is normalized to [slowSaltLength] bytes when used.
     */
    var salt: ByteArray? = null
    /** Number of iterations (work factor) for the slow hash. */
    var iterations: Int? = null
    /** Memory cost, in kilobytes, for memory-hard slow hashes (such as Argon2). */
    var memory: Int? = null
    /** Degree of parallelism (lanes) for the slow hash (such as Argon2). */
    var parallelism: Int? = null
    /**
     * Optional pepper mixed into the slow-hash derivation. It is not stored with the hash, so the same
     * value must be configured to verify a password later.
     */
    var seed: ByteArray? = null

    public override fun clone(): HashParams {
        val clone = super.clone() as HashParams
        // Deep copy the mutable arrays, so callers cannot mutate the original through the clone.
        clone.salt = salt?.copyOf()
        clone.seed = seed?.copyOf()
        return clone
    }
}
