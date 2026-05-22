package dev.strategia.aplino.error

import dev.strategia.aplino.event.AppEvent
import dev.strategia.aplino.event.EventListener
import java.io.Serializable
import java.util.UUID

/**
 * Base implementation of error service.
 */
open class BaseErrorService : ErrorService {
    protected var errors = mutableMapOf<String, ErrorInfo?>()
    protected var loader: ErrorInfoProvider? = null

    constructor(provider: ErrorInfoProvider? = null) {
        if (provider != null) {
            loader = provider
        }
    }

    override fun start() {
        if (loader == null) {
            loader = createErrorInfoProvider()
        }
    }

    override fun throwError(origin: Any, errorCode: String, details: String, exception: Throwable?,
                            context: Map<String, Any?>?, vararg detailsParams: Serializable?) {
        // Prepare error info holder.
        var error: ErrorInfo? = getErrorInfo(errorCode)
        if (error == null) {
            error = ErrorInfo(errorCode)
        }
        error.details = details
        if (detailsParams.isNotEmpty()) {
            error.detailsParams = arrayOf(detailsParams)
        }
        error.context = context
        error.origin = origin
        error.ticket = generateTicketId()
        fillErrorInfo(error)

        handleError(error, exception)
    }

    override fun handleError(listener: EventListener, exception: Throwable, event: AppEvent): ErrorResponse {
        val response = ErrorResponse(ErrorPolicy.Raise)
        return response
    }

    /**
     * Get error information for specified error.
     * @param errorCode The error code.
     * @return Error information. Null if such info does not exist.
     */
    protected open fun getErrorInfo(errorCode: String): ErrorInfo? {
        var error: ErrorInfo? = errors[errorCode]
        if (error == null) {
            error = loader?.load(errorCode)
            errors[errorCode] = error
        }
        if (error != null) {
            error = error.clone()
        }
        return error
    }

    /**
     * Handle specified error. This is extension point.
     * @param error The error.
     * @param exception Occurred exception (if any).
     */
    protected open fun handleError(error: ErrorInfo, exception: Throwable?) {
        throwException(error, exception)
    }

    /**
     * Generate unique error ticket id (easy to be located in logs).
     * @return Generated identifier.
     */
    protected open fun generateTicketId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Raise application exception for specified error.
     * @param error The error.
     * @param exception Occurred exception (if any).
     *
     * @throws ApplicationException to signal what happened.
     */
    protected open fun throwException(error: ErrorInfo, exception: Throwable?) {
        throw ApplicationException(error, exception)
    }

    /**
     * Fill additional error information (like help URL, which is application specific).
     * This is extension point.
     * @param error Error info holder.
     */
    protected open fun fillErrorInfo(error: ErrorInfo) {
    }

    /**
     * Create the default error information loader.
     * @return Loader instance.
     */
    protected open fun createErrorInfoProvider(): ErrorInfoProvider {
        return MapErrorInfoProvider()
    }
}
