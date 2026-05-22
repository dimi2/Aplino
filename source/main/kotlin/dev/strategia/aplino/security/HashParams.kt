package dev.strategia.aplino.security

/**
 * Holder for hash generation parameters.
 */
open class HashParams: Cloneable {
    var fastHashAlgorithm: String? = null
    var slowHashAlgorithm: String? = null
    var keyAlgorithm: String? = null
    var slowHashLength: Int? = null
    var slowSaltLength: Int? = null
    var salt: ByteArray? = null
    var iterations: Int? = null
    var memory: Int? = null
    var parallelism: Int? = null
    var seed: ByteArray? = null

    public override fun clone(): HashParams {
        return super.clone() as HashParams
    }
}
