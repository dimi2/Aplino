package dev.strategia.aplino.event

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.LoggerConfig

/**
 * Log level modifier. Dynamically change the log level for classes, associated with given exception.
 */
open class LogLevelModifier {
    protected val modified = mutableMapOf<String, Level>()
    protected var maxModified = 5
    protected var excludedClasses: List<String>

    constructor() {
        excludedClasses = buildExcludedClasses()
    }

    fun increaseLogLevels(exception: Throwable) {
        val logContext = LogManager.getContext(false) as LoggerContext
        val conf = logContext.configuration
        val classes = getExtLogClasses(exception)
        for (className in classes) {
            val parentLogger = conf.getLoggerConfig(className)
            if (!modified.containsKey(className)) {
                // Store the original log level.
                modified[className] = parentLogger.level
            }
            // Create custom logger for the affected class (with extended log level).
            val loggerConfig = LoggerConfig.newBuilder().setLevel(Level.TRACE).setLoggerName(className)
                .setRefs(parentLogger.appenderRefs.toTypedArray()).setAdditivity(parentLogger.isAdditive)
                .setConfig(conf).build()
            conf.addLogger(loggerConfig.name, loggerConfig)
        } //
        logContext.updateLoggers(conf)
    }

    fun restoreLogLevels() {
        val logContext = LogManager.getContext(false) as LoggerContext
        val conf = logContext.configuration
        for (entry in modified.entries) {
            val loggerConfig = conf.getLoggerConfig(entry.key)
            loggerConfig.level = entry.value
            conf.removeLogger(loggerConfig.name)
        } //
        logContext.updateLoggers(conf)
    }

    protected open fun getExtLogClasses(exception: Throwable): Set<String> {
        val classes = mutableSetOf<String>()
        val stackTrace = exception.stackTrace
        for (i in stackTrace.indices) {
            val frame = stackTrace[i]
            val className = frame.className
            var isExcluded = false
            for (excludedClass in excludedClasses) {
                if (className.startsWith(excludedClass)) {
                    isExcluded = true
                    break
                }
            } //
            if (!isExcluded) {
                classes.add(className)
                if (classes.size >= maxModified) {
                    break
                }
            }
        } //
        return classes
    }

    protected fun buildExcludedClasses(): MutableList<String> {
        return mutableListOf("java.", "javax.", "jdk.internal.", "com.sun.", "org.junit.", "com.intellij.")
    }

}
