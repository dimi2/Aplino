package dev.strategia.aplino.log

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.message.MessageFactory
import org.apache.logging.log4j.spi.AbstractLogger
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom implementation of the Logger that writes messages to the console.
 * Using this class directly (outside LogManager) will not trigger changes in application logging context.
 * Use this class when the logging system is not yet initialized, but you need logging.
 */
open class ConsoleLogger(caller: Any?, messageFactory: MessageFactory? = null) :
    AbstractLogger(caller?.javaClass?.name ?: "root", messageFactory) {

    protected var configLevel: Level = Level.DEBUG

    override fun isEnabled(level: Level?, marker: Marker?): Boolean {
        return level != null && level.intLevel() <= configLevel.intLevel()
    }

    override fun logMessage(fqcn: String, level: Level, marker: Marker?, message: Message,
                            throwable: Throwable?) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        val threadName = Thread.currentThread().name
        val markerStr = marker?.let { "${it.name} " } ?: ""
        val formattedMessage = message.formattedMessage
        val throwableStr = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            "\n${sw.toString().trim()}"
        } ?: ""

        val logLine = "$timestamp [$threadName] ${level.name()} ${getName()} " +
            "$markerStr- $formattedMessage$throwableStr"
        System.err.println(logLine)
    }

    //----- Dummy implementation to satisfy the interface requirements.

    override fun isEnabled(level: Level?, marker: Marker?, message: Message?, t: Throwable?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: CharSequence?, t: Throwable?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: Any?, t: Throwable?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, t: Throwable?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, vararg params: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?, p3: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?, p3: Any?, p4: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?, p3: Any?, p4: Any?, p5: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?, p3: Any?, p4: Any?, p5: Any?, p6: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?, p3: Any?, p4: Any?, p5: Any?, p6: Any?, p7: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?,
                           p2: Any?, p3: Any?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun isEnabled(level: Level?, marker: Marker?, message: String?, p0: Any?, p1: Any?, p2: Any?,
                           p3: Any?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?, p9: Any?): Boolean {
        return isEnabled(level, marker)
    }

    override fun getLevel(): Level {
        return configLevel
    }
}
