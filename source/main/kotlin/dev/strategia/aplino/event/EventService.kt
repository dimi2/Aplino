package dev.strategia.aplino.event

import dev.strategia.aplino.application.AppService
import kotlin.reflect.KClass

/**
 * Application event dispatch service. It provides decoupled communication between application components.
 * The component can send event. Registered listeners will process the event asynchronously.
 */
interface EventService : AppService {

    /**
     * Register application event listener.
     * @param listener The event listener.
     * @param withPriority True if the listener should be added in front of the other listeners
     * (and be notified first when event comes).
     * @param forEvents Names of the events to listen. If this is not provided, the listener class name will
     * be used as default event name.
     * @see removeEventListener
     */
    fun addEventListener(listener: EventListener, withPriority: Boolean,
                        vararg forEvents: KClass<out AppEvent>
    )

    /**
     * Get list of all events which has registered listeners.
     * @return Event names list (can be empty).
     * @see addEventListener
     */
    fun getRegisteredEvents(): Set<KClass<out AppEvent>>

    /**
     * Get list of listeners, registered for specified event.
     * Note: changing the returned list will not change the registered listeners.
     * @param forEvent The event of interest. Null means 'all'.
     * @return List of event listeners.
     * @see addEventListener
     */
    fun getEventListeners(forEvent: KClass<out AppEvent>?): Set<EventListener>

    /**
     * Removes registered event listener.
     * @param listener The listener to be removed.
     * @param forEvents List of event names from which to remove the listener. Null means 'all'.
     * @see addEventListener
     */
    fun removeEventListener(listener: EventListener, vararg forEvents: KClass<out AppEvent>)

    /**
     * Sends event to all registered event listeners and forget about it.
     * The method will return without waiting the listeners to process the event.
     * @param event The event to send.
     * @see process
     */
    fun send(event: AppEvent)

    /**
     * Send event to all registered event listeners and wait them to process it.
     * The method will return after all listeners process the event.
     * @param event The event to be processed.
     * @see send
     */
    fun process(event: AppEvent)
}
