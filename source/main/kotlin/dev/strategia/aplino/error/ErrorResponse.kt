package dev.strategia.aplino.error

import java.io.Serializable

/**
 * Holder for error handling response.
 */
open class ErrorResponse : Serializable {
    /** How to handle the error. */
    var errorPolicy: ErrorPolicy
    /** Strategy to use in case of action reties. */
    var retryStrategy: RetryStrategy? = null

    constructor(strategy: ErrorPolicy, retry: RetryStrategy? = null) {
        this.errorPolicy = strategy
        this.retryStrategy = retry
    }
}
