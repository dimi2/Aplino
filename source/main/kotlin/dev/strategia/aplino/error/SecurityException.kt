package dev.strategia.aplino.error

/**
 * Security exception for data access errors (access denied).
 * The user does not have necessary permissions to perform given action.
 */
open class SecurityException : BaseException {

    constructor(details: String) : super(details)

    constructor(details: String, exception: Throwable?) : super(details, exception)

    constructor(errorInfo: ErrorInfo, exception: Throwable?) : super(errorInfo, exception)
}
