package dev.strategia.aplino.error

import java.io.Serializable

/**
 * Error handling policy.
 */
enum class ErrorPolicy : Serializable {
    /** Ignore the error. */
    Ignore,
    /** Log the error. */
    Log,
    /** Raise (throw) the error further. */
    Raise,
    /** Retry the event, which caused the error. */
    Retry
}
