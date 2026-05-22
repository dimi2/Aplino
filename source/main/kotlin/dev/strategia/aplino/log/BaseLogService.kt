package dev.strategia.aplino.log

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

/**
 * Base implementation of logging service. It uses [Apache Log4j](https://logging.apache.org/log4j/2.x/)
 * library, which is reconfigurable at runtime.
 */
open class BaseLogService : LogService {
    protected var paramConfigFile = "log4j.configurationFile"
    protected var logConfigFile: String? = null

    constructor(configFile: String? = null) {
        this.logConfigFile = configFile
    }

    override fun getLogger(caller: Any?): Logger {
        return LogManager.getLogger(caller)
    }

    override fun start() {
        // The log config should be pointed globally, before any call of the log service.
        if (logConfigFile != null) {
            // Is the logging config file overridden via system property?
            val conf = System.getProperty(paramConfigFile)
            if (conf != null) {
                logConfigFile = conf
            }
        }

        Configurator.initialize(null, logConfigFile)
        val logger = LogManager.getLogger(this)
        if (logger.isDebugEnabled) {
            logger.debug("Log service configured from: $logConfigFile")
            logger.debug("Log level: ${logger.level.name()}")
        }
    }

    override fun stop() {
        LogManager.shutdown()
    }

}
