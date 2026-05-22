package dev.strategia.aplino.event

import java.util.UUID

/**
 * Base implementation of application event.
 */
open class BaseAppEvent : AppEvent {
    override lateinit var name: String
    override lateinit var traceId: String
    override var readOnly: Boolean? = null
    override var replayable: Boolean? = null
    override var payload: Any? = null

    constructor(name: String, traceId: String? = null, readOnly: Boolean? = null,
                replayable: Boolean? = null, payload: Any? = null) {
        this.name = name
        if (traceId == null) {
            this.traceId = generateTraceId()
        }
        else {
            this.traceId = traceId
        }
        this.readOnly = readOnly
        this.replayable = replayable
        this.payload = payload
    }

    override fun toString(): String {
        return "$name($traceId)"
    }

    protected open fun generateTraceId(): String {
        return UUID.randomUUID().toString()
    }
}
