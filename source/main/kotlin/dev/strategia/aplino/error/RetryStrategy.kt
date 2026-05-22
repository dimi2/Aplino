package dev.strategia.aplino.error

import java.io.Serializable

/**
 * Action retry strategy. Allows to handle dynamically different retry schemes.
 */
interface RetryStrategy : Serializable, Cloneable {

    /**
     * Get the count of retry attempts.
     * @return How many times to retry. -1 means "no limit".
     * @see getCurrentAttempt
     */
    fun getMaxAttempts(): Long

    /**
     * Get the current attempt number.
     * @see getMaxAttempts
     */
    fun getCurrentAttempt(): Long

    /**
     * Get the delay before the retry attempt.
     * @return Delay in milliseconds.
     */
    fun getDelayToNextAttempt(): Long

    /**
     * Mark a performed attempt (incremental counter).
     * @see getCurrentAttempt
     */
    fun markAttempt()
}
