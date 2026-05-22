package dev.strategia.aplino.error

/**
 * Loader for predefined error information records. It separates the error handling from the error
 * descriptions and how these descriptions are stored.
 */
interface ErrorInfoProvider {
    /**
     * Load the error information for specified error.
     * @param errorCode Error code.
     * @return Information for this error.
     */
    fun load(errorCode: String): ErrorInfo?

    /**
     * Store specified error information.
     * @param errorInfo Information to be stored.
     */
    fun store(errorInfo: ErrorInfo)
}
