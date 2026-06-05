package dev.strategia.aplino.application

import dev.strategia.aplino.application.App.Companion.get
import dev.strategia.aplino.application.App.Companion.mode
import dev.strategia.aplino.config.ConfigService
import dev.strategia.aplino.error.ErrorService
import dev.strategia.aplino.event.EventService
import dev.strategia.aplino.log.ConsoleLogger
import dev.strategia.aplino.log.LogService
import dev.strategia.aplino.security.DataEncryptor
import org.apache.logging.log4j.Logger
import java.io.Serializable
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

/**
 * The application context - provides centralized access to common application functionality. Functionalities:
 * - Execution mode ([AppConstant.PRODUCTION], [AppConstant.DEVELOPMENT], ...): [App.mode].
 * - Centralized logger service: [App.log].
 * - Application configuration service: [App.config].
 * - Centralized error handling: [App.error].
 * - Application event dispatcher service: [App.event].
 * - Global application registry (for extended functionality): [App.get].
 * - Automatic detection of the home directory. All the relative paths starts from there.
 *
 * The application context is initialized via [AppBootstrap], where the necessary application services are
 * prepared and customized.
 */
class App {
    companion object {
        private var mode = AppConstant.PRODUCTION

        private var defLocale = "en"

        private var homeDirectory: String = ""

        private lateinit var encryptor: DataEncryptor
        private lateinit var logService: LogService
        private lateinit var configService: ConfigService
        private lateinit var errorService: ErrorService
        private lateinit var eventService: EventService

        private val registry = ConcurrentHashMap<String, Any>()
        private val loggerRegistry = ConcurrentHashMap<String, ConsoleLogger>()

        /**
         * Get the application home directory. Useful to construct full file paths from relative paths.
         * This is the work directory.
         * @return The directory where the application is installed.
         */
        fun home(): String {
            return homeDirectory
        }

        /**
         * Get the default application logger - centralized.
         * @param logSource Log source object.
         * @return Logger instance.
         */
        fun log(logSource: Any? = null): Logger {
            var res: Logger
            try {
                res = logService.getLogger(logSource)
            } catch (_: UninitializedPropertyAccessException) {
                // The application logging have not been initialized. Could happen in unit tests.
                val loggerName = logSource?.toString() ?: "App"
                res = loggerRegistry.getOrPut(loggerName) {
                    val newLogger = ConsoleLogger(logSource)
                    if (loggerRegistry.isEmpty()) {
                        newLogger.warn("Logging service was not initialized during bootstrap." +
                            " Using internal logger (basic implementation).")
                    }
                    newLogger
                }
            }
            return res
        }

        /**
         * Get the application logging service. Allows to modify the logging at runtime.
         * @return Logging service.
         */
        fun logging(): LogService {
            return logService
        }

        /**
         * Get the application encryption service.
         * @return Encryption service.
         */
        fun encryption(): DataEncryptor {
            return encryptor
        }

        /**
         * Get application config service. It handles the application configuration.
         * @return Service instance.
         */
        fun config(): ConfigService {
            return configService
        }

        /**
         * Get the application error service. It is used to handle errors in centralized place.
         * @return Service instance.
         */
        fun error(): ErrorService {
            return errorService
        }

        /**
         * Throw application error. It is used to handle errors in centralized place. Strive to avoid direct
         * creation of exceptions everywhere inside the code and throwing them without a chance to mediate.
         * @param origin The object which throws the error.
         * @param errorCode Code of the occurred error (from predefined errors list).
         * @param details Custom error details.
         * @param exception Occurred exception (if any).
         * @param context Additional error context objects for debugging.
         * @param detailsParams Additional parameters for the error details placeholders.
         * @throws Exception ApplicationException, eventually wrapping the original exception.
         */
        fun throwError(origin: Any, errorCode: String, details: String, exception: Throwable? = null,
                       context: Map<String, Any?>? = null, vararg detailsParams: Serializable?) {
            errorService.throwError(origin, errorCode, details, exception, context, *detailsParams)
        }

        /**
         * Get the application event dispatch service. It is like event bus, providing decoupled
         * communication inside/outside the application.
         * @return Service instance.
         */
        fun event(): EventService {
            return eventService
        }

        /**
         * Get reference object instance from the application registry.
         * @param key Reference key name.
         * @return The requested object reference.
         * @see set
         */
        fun get(key: String): Any? {
            var obj = registry[key]
            if (obj is Callable<*>) {
                // Delayed value retrieval (on demand).
                obj = obj.call()
                if (obj != null) {
                    // Cache the loaded value for next time.
                    registry[key] = obj
                }
            }
            else if (obj is Supplier<*>) {
                // The value is generated on demand.
                obj = obj.get()
            }
            return obj
        }

        /**
         * Get the default application locale.
         * @return Default locale.
         */
        fun getDefaultLocale(): String {
            return defLocale
        }

        /**
         * Get the application execution mode.
         * @return Could be [AppConstant.PRODUCTION], [AppConstant.DEVELOPMENT] etc.
         * @see mode
         */
        fun mode(): String {
            return mode
        }

    }

    fun setDefaultLocale(locale: String) {
        defLocale = locale
    }

    fun setHomeDirectory(directory: String) {
        homeDirectory = directory
    }

    fun setEncryptor(impl: DataEncryptor) {
        encryptor = impl
    }

    fun setLogService(impl: LogService) {
        logService = impl
    }

    fun setConfigService(impl: ConfigService) {
        configService = impl
    }

    fun setErrorService(impl: ErrorService) {
        errorService = impl
    }

    fun setEventService(impl: EventService) {
        eventService = impl
    }

    /**
     * Set a value to the application registry. The registry is used to store application-wide
     * objects, like services, user profiles etc.
     *
     * Example for usage:
     * ```
     *    application.set("userService", UserService()) // immediate loading
     *    application.set("brokerAccount", Callable { loadBrokerAccount() }) // on demand loading
     * ```
     *
     * @param key Reference key name.
     * @param value Value to be stored. If the value is a [Callable], it will be called and the result
     * will be stored in the registry (kind of on-demand loading). If the value is a [Supplier], it
     * will be called each time and the result will be returned.
     * @return The previous value associated with this key.
     * @see get
     */
    fun set(key: String, value: Any): Any? {
        return registry.put(key, value)
    }

    /**
     * Set the application execution mode.
     * @param modeName Mode name. Could be [AppConstant.PRODUCTION], [AppConstant.DEVELOPMENT] etc.
     * @see [AppConstant.DEVELOPMENT]
     * @see [AppConstant.PRODUCTION]
     */
    fun setMode(modeName: String) {
        mode = modeName
    }

}
