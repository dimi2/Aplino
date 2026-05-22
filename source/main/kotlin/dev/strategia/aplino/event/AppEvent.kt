package dev.strategia.aplino.event

import java.io.Serializable

/**
 * Common interface for application events.
 */
interface AppEvent : Serializable {
    /** Event name. */
    var name: String
    /** Unique trace identifier (to track event handling inside distributed workflows). */
    var traceId: String
    /** Is it supposed only to read (query) the application data? */
    var readOnly: Boolean?
    /** Could the event be replay-ed? */
    var replayable: Boolean?
    /** Additional event data (affected object). */
    var payload: Any?
}
