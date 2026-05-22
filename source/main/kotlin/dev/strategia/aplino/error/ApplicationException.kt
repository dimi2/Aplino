package dev.strategia.aplino.error

/**
 * Application execution exception (something wrong happened at runtime).
 */
open class ApplicationException : BaseException {

    constructor(details: String) : super(details)

    constructor(details: String, exception: Throwable?) : super(details, exception)

    constructor(errorInfo: ErrorInfo, exception: Throwable?) : super(errorInfo, exception)
}
