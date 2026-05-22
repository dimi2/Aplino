package dev.strategia.aplino.config

/**
 * Interface for application configuration providers (which read and write configuration).
 * It detaches the config service from the storage format of the settings.
 * The format could be YAML, XML, Properties...
 */
interface ConfigProvider {

    /**
     * Load the configuration.
     * @param configLocation The location of the configuration file (typically - file name).
     * @return The loaded config data.
     * @see store
     */
    fun load(configLocation: String): ConfigHolder

    /**
     * Store the configuration.
     * @param configLocation The location of the configuration file (typically - file name).
     * @param config Config data to be stored.
     * @see load
     */
    fun store(configLocation: String, config: ConfigHolder)
}
