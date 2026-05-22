package dev.strategia.aplino.event

/**
 * Common interface for application event listeners.
 */
interface EventListener {
    /**
     * Called, when arrives and event, for which this listener is registered.
     * @param event The event to be handled.
     * @see abort
     */
    fun handle(event: AppEvent)

    /**
     * Called, when a previously handled event changes must be aborted (error happened in event processing
     * after execution of this listener). The changes may include caches, file changes, external system
     * signals etc.
     * If called during long-running event handling, this is signal to abort it.
     * @see handle
     */
    fun abort(event: AppEvent)
}
