package dev.strategia.aplino.log

import dev.strategia.aplino.application.AppService
import dev.strategia.aplino.log.LogService.Companion.LEVEL_AUDIT
import dev.strategia.aplino.log.LogService.Companion.MARKER_AUDIT
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.MarkerManager

/**
 * Application logging service interface. It is bound to proven and powerful Logging library -
 * [Apache Log4j](https://logging.apache.org/log4j/2.x/) which is technically superior to the current
 * alternatives.
 *
 * To create audit logs, use log level [LEVEL_AUDIT] and (optionally) add marker [MARKER_AUDIT] ot the
 * log statement:
 * ```
 * logger.log(LogService.LEVEL_AUDIT, LogService.MARKER_AUDIT, "My audit message`.)
 * ```
 * This allows to redirect the audit logs on separate location (in the logging configuration) and
 * do not depend on the current log level. Audit logs are often a regulatory requirement.
 */
interface LogService : AppService {
    companion object {
        /**
         * Custom logging level for audit logs.
         * It is higher from ERROR and will be logged (almost) always.
         */
        val LEVEL_AUDIT = Level.forName("AUDIT", 150)!!
        /** Dedicated audit log marker. */
        val MARKER_AUDIT = MarkerManager.getMarker(LEVEL_AUDIT.name())!!
    }

    /**
     * Get the application logger for specified caller/class.
     * @param caller The log caller (for later identification). Typically `this`.
     * @return Logger instance for this caller.
     */
    fun getLogger(caller: Any? = null): Logger

}
