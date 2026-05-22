package dev.strategia.aplino.error

/**
 * Exception for communication and data transfer errors (physical read/write problems).
 * This could be temporary error, resolvable without program code change.
 */
open class DataTransferException : BaseException {

    constructor(details: String) : super(details)

    constructor(details: String, exception: Throwable?) : super(details, exception)

}
