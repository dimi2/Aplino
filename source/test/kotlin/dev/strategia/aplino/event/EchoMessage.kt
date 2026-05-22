package dev.strategia.aplino.event

/**
 * Echo message event (for test purposes).
 */
internal class EchoMessage() : BaseAppEvent("EchoMessage") {
    var message: String? = null

    constructor(message: String): this() {
        this.message = message
    }
}
