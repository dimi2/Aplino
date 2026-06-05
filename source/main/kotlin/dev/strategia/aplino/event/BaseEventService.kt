package dev.strategia.aplino.event

import dev.strategia.aplino.error.ApplicationException
import dev.strategia.aplino.error.ErrorPolicy
import dev.strategia.aplino.error.ErrorResponse
import dev.strategia.aplino.error.ErrorService
import dev.strategia.aplino.error.Errors
import dev.strategia.aplino.error.RetryStrategy
import dev.strategia.aplino.log.LogService
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.reflect.KClass

/**
 * Default implementation of application event dispatcher service.
 */
open class BaseEventService : EventService {
    protected var listeners = ConcurrentSkipListMap<KClass<out AppEvent>, MutableList<EventListener>>(
        KClassComparator())
    protected lateinit var executor: ExecutorService
    protected val logService: LogService?
    protected val errorService: ErrorService?

    constructor(logService: LogService? = null, errorService: ErrorService? = null) {
        this.logService = logService
        this.errorService = errorService
    }

    override fun addEventListener(listener: EventListener, withPriority: Boolean,
                                  vararg forEvents: KClass<out AppEvent>) {
        // Add the listener.
        synchronized(listeners) {
            for (event in forEvents) {
                // Get the listeners list for this event.
                var list = listeners[event]
                if (list == null) {
                    // No such list yet. Create it.
                    list = mutableListOf()
                    listeners[event] = list
                }
                var position = 0
                if (!withPriority) {
                    // Add to the end of the list.
                    position = list.size
                }
                list.add(position, listener)
            } //
        }
    }

    override fun getRegisteredEvents(): Set<KClass<out AppEvent>> {
        return LinkedHashSet(listeners.keys)
    }

    override fun getEventListeners(forEvent: KClass<out AppEvent>?): Set<EventListener> {
        val ret = linkedSetOf<EventListener>()
        if (forEvent != null) {
            // Return the listeners for this event.
            val eh = listeners[forEvent]
            if (eh != null) {
                ret.addAll(eh)
            }
        } else {
            // Return the listeners for all events.
            for (entry in listeners.entries) {
                ret.addAll(entry.value)
            } //
        }
        return ret
    }

    override fun removeEventListener(listener: EventListener, vararg forEvents: KClass<out AppEvent>) {
        synchronized(listeners) {
            val eventsList = if (forEvents.isEmpty()) {
                // Stop listening for all events.
                listeners.keys.iterator()
            } else {
                // Stop listening only for the provided events.
                forEvents.iterator()
            }
            while (eventsList.hasNext()) {
                val event = eventsList.next()
                // Get the listeners for this event.
                val eventListeners = listeners[event]
                if (eventListeners != null) {
                    eventListeners.remove(listener)
                    if (eventListeners.isEmpty()) {
                        // No more listeners for this event. Remove it.
                        listeners.remove(event)
                    }
                }
            } //
        }
    }

    override fun send(event: AppEvent) {
        // Process the event asynchronously in other thread.
        executor.execute { process(event) }
    }

    override fun process(event: AppEvent) {
        val logger = logService?.getLogger(this)
        // Get the listeners for this event.
        if (logger != null && logger.isInfoEnabled) {
            logger.info("Begin processing event: $event")
        }
        val listenerList = listeners[event::class]
        if (listenerList != null) {
            var retryStrategy: RetryStrategy? = null
            val logMod = LogLevelModifier()
            var retries = 0
            // Notify the listeners about the event.
            var i = 0
            while (i < listenerList.size) {
                val listener = listenerList[i]
                try {
                    listener.handle(event)
                }
                catch (exc: Exception) {
                    // Error occurred. Discover how to handle it.
                    val errorResponse = handleError(listener, exc, event)
                    when (errorResponse.errorPolicy) {
                        ErrorPolicy.Raise -> {
                            // Raise error and notify previous listeners to roll back any event related
                            // changes they have made.
                            raiseError(exc, event, listener, listenerList.subList(0, i))
                        }
                        ErrorPolicy.Log -> {
                            // Just log the error.
                            logger?.error("Failed to handle event: [$event] in " + listener.javaClass, exc)
                        }
                        ErrorPolicy.Retry -> {
                            // Retry the event.
                            if (retryStrategy == null) {
                                retryStrategy = errorResponse.retryStrategy
                            }
                            if (retryStrategy == null) {
                                // Retry requested but no retry strategy provided - the retries cannot be
                                // bounded, so log the error and continue instead of looping forever.
                                logger?.error("Retry requested without a retry strategy for event:" +
                                    " [$event] in " + listener.javaClass + ". Skipping the listener.", exc)
                            }
                            else {
                                // Mark retry attempt.
                                retryStrategy.markAttempt()
                                if (retryStrategy.getCurrentAttempt() >= retryStrategy.getMaxAttempts()) {
                                    // Maximum attempts exceeded - give up and notify previous listeners to
                                    // roll back any event related changes they have made.
                                    raiseError(exc, event, listener, listenerList.subList(0, i))
                                }
                                // Wait some time before the retry.
                                val delay = retryStrategy.getDelayToNextAttempt()
                                Thread.sleep(delay)
                                // Increase the log level before retrying.
                                retries = 1
                                logMod.increaseLogLevels(exc)
                                // Reiterate event processing with same listener.
                                i--
                            }
                        }
                        ErrorPolicy.Ignore -> {
                            // Ignore the error.
                        }
                    }
                }
                finally {
                    if (retries == 1) {
                        // Log level is raised, but the listener did not retry the event yet.
                        retries = 2
                    }
                    else if (retries == 2) {
                        // The listener retried the event. Restore the log level.
                        logMod.restoreLogLevels()
                        retries = 0
                    }
                }
                // Continue with the next listener.
                i++
            } //
        }
        if (logger != null && logger.isInfoEnabled) {
            logger.info("End processing event: $event")
        }
    }

    override fun start() {
        executor = startAsyncExecutor()
    }

    override fun stop() {
        stopAsyncExecutor()
    }

    /**
     * Handle event processing error.
     * @param listener The listener which rise the error.
     * @param exception The exception occurred.
     * @param event The event on which the error happened.
     * @return Error response holder.
     */
    protected open fun handleError(listener: EventListener, exception: Throwable,
                                   event: AppEvent): ErrorResponse {
        val res: ErrorResponse
        if (errorService != null) {
            res = errorService.handleError(listener, exception, event)
        }
        else {
            res = ErrorResponse(ErrorPolicy.Raise)
        }
        return res
    }

    /**
     * Send abort signal for given event to list of listeners.
     * @param event The event to be aborted.
     * @param listeners Listeners list (to be notified).
     */
    protected open fun abortEvent(event: AppEvent, listeners: MutableList<EventListener>) {
        val logger = logService?.getLogger(javaClass)
        if (logger != null && logger.isInfoEnabled) {
            logger.info("Begin aborting event: $event")
        }
        listeners.forEach { listener ->
            try {
                listener.abort(event)
            }
            catch (e: Exception) {
                // Log the error, but do not stop notifying of other listeners.
                // One listener may fail, but the rest may pass.
                logger?.error("Failed aborting event for listener: $listener", e)
            }
        }
        if (logger != null && logger.isInfoEnabled) {
            logger.info("End aborting event: $event")
        }
    }

    /**
     * Raise event handling error.
     * @throws RuntimeException containing the event error.
     */
    protected open fun raiseError(exception: Throwable, event: AppEvent, listener: EventListener,
                                  prevListeners: MutableList<EventListener>) {
        // Allow previously called listeners to roll back their event-induced changes.
        abortEvent(event, prevListeners)
        // Throw the occurred error.
        if (errorService != null) {
            errorService.throwError(this, Errors.EVENT_HANDLING_FAILURE.toString(),
                "Failed to handle event: [$event] in " + listener.javaClass, exception)
        }
        else {
            throw ApplicationException("Application event failed", exception)
        }
    }

    /**
     * Create asynchronous executor for event handling service.
     * @return Executor service.
     */
    protected open fun startAsyncExecutor(): ExecutorService {
        return Executors.newVirtualThreadPerTaskExecutor()
    }

    /**
     * Shut down the asynchronous executor. This will stop accepting new executions and
     * may need some time to finish the executions which are currently in progress.
     */
    protected open fun stopAsyncExecutor() {
        executor.shutdown()
    }

    class KClassComparator : Comparator<KClass<*>> {
        override fun compare(o1: KClass<*>, o2: KClass<*>): Int {
            var ret = 0
            if (o1 != o2) {
                ret = o1.simpleName!!.compareTo(o2.simpleName!!)
            }
            return ret
        }
    }

}
