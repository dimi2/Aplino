package dev.strategia.aplino.error

/**
 * Exception for data structure errors (communication protocol violations).
 * This error cannot be resolved without program code change.
 */
open class DataStructureException : BaseException {

    constructor(details: String) : super(details)

    constructor(details: String, exception: Throwable?) : super(details, exception)

}
