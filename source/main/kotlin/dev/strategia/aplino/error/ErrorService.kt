package dev.strategia.aplino.error

import dev.strategia.aplino.application.AppService
import dev.strategia.aplino.event.AppEvent
import dev.strategia.aplino.event.EventListener
import java.io.Serializable

/**
 * Application error service, which provides centralized error handling. We do not want exceptions to be
 * crated and thrown from everywhere: using same classes and without context.
 * The application should be able to learn from the errors, adapt and eventually recover.
 */
interface ErrorService : AppService {

    /**
     * Throw an application error (after collecting some debug information).
     * @param origin The object which throws the error.
     * @param errorCode Code of the occurred error (from predefined errors list).
     * @param details Custom error details.
     * @param exception Occurred exception (if any).
     * @param context Additional error context objects for debugging.
     * @param detailsParams Additional parameters for the error details placeholders.
     * @throws Exception ApplicationException, eventually wrapping the original exception.
     */
    fun throwError(origin: Any, errorCode: String, details: String, exception: Throwable? = null,
                   context: Map<String, Any?>? = null, vararg detailsParams: Serializable?)

    /**
     * Handle event processing error (custom error recovery/diagnostic activities could be performed here).
     * @param listener The listener which rise the error.
     * @param exception The exception occurred.
     * @param event The event on which the error happened.
     * @return Recommended error response.
     */
    fun handleError(listener: EventListener, exception: Throwable, event: AppEvent): ErrorResponse
}
