package dev.strategia.aplino.application

/**
 * Common interface for application services.
 */
interface AppService {

    /**
     * Start the service.
     * @see stop
     */
    fun start() {}

    /**
     * Stop the service.
     * @see start
     */
     fun stop() {}
}
