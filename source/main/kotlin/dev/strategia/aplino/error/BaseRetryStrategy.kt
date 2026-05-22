package dev.strategia.aplino.error

/**
 * Base action retry strategy.
 */
open class BaseRetryStrategy : RetryStrategy {
    protected val max: Long
    protected var interval: Long
    protected var attempts = 0L

    constructor(maxAttempts: Long, intervalMs: Long = 0) {
        this.max = maxAttempts
        this.interval = intervalMs
    }

    override fun getMaxAttempts(): Long {
        return max
    }

    override fun getCurrentAttempt(): Long {
        return attempts
    }

    override fun getDelayToNextAttempt(): Long {
        return interval
    }

    override fun markAttempt() {
        attempts++
    }

    override fun toString(): String {
        return "(maxAttempts=$max, interval=$interval, attempts=$attempts)"
    }

}
