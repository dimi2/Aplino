package dev.strategia.aplino.error

/**
 * Configuration exception, cause by wrong config settings.
 */
open class ConfigurationException : BaseException {

    constructor(details: String) : super(details)

    constructor(details: String, exception: Throwable?) : super(details, exception)

    constructor(errorInfo: ErrorInfo, exception: Throwable?) : super(errorInfo, exception)
}
