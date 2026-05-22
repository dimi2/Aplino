package dev.strategia.aplino.event

import dev.strategia.aplino.log.LogService

/**
 * Message listener which returns the received message back (used for test purposes).
 */
internal class EchoMessageListener : EventListener {
    var called = 0
    var stopHandling = false
    val logService: LogService?

    constructor(logService: LogService? = null) {
        this.logService = logService
    }

    override fun handle(event: AppEvent) {
        called++
        if (logService != null) {
            logService.getLogger(this).trace("EchoMessageListener called: $called")
        }
        if (stopHandling) {
            throw EchoTestException("Stop handling $event")
        }
    }

    override fun abort(event: AppEvent) {
    }

    // Use exception with 'Test' inside the name to reduce confusion when looking at test output.
    class EchoTestException : IllegalStateException {

        constructor(message: String): super(message)
    }
}
