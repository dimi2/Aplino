package dev.strategia.aplino.config

import dev.strategia.aplino.application.AppService

/**
 * Interface for application configuration service.
 */
interface ConfigService : AppService {

    /**
     * Set configuration setting value.
     * @param key Setting key.
     * @return Setting value or null.
     * @see getValue
     */
    fun setValue(key: String, value: Any?)

    /**
     * Get configuration setting value (raw object).
     * @param key Setting key.
     * @return Setting value or null.
     * @see getValueMandatory
     * @see getValues
     */
    fun getValue(key: String, defaultValue: Any? = null): Any?

    /**
     * Get configuration setting value (raw object) for mandatory configuration.
     * @param key Setting key. Can contain group path (like '/ui/themes/fontSize').
     * @return Setting value. Throws exception if the configuration does not contain such value.
     * @throws IllegalArgumentException if the configuration value is not set.
     * @see getValue
     * @see getValues
     */
    @Throws(IllegalArgumentException::class)
    fun getValueMandatory(key: String): Any

    /**
     * Get list of configuration values (raw objects).
     * @param keyPattern Regex selector for the required setting names. Null means 'all'.
     * @return Setting value or null.
     * @see getValue
     * @see getValueMandatory
     */
    fun getValues(keyPattern: String? = null): Map<String, Any?>

    /**
     * Set configuration setting.
     * @param key Setting key.
     * @return The setting (or null, to remove existing setting).
     * @see getValue
     */
    fun setSetting(key: String, setting: ConfigSetting?)

    /**
     * Get specified configuration setting.
     * @param key Setting key.
     * @return The setting or null.
     * @see getValue
     * @see getValueMandatory
     * @see getSettingKeys
     */
    fun getSetting(key: String): ConfigSetting?

    /**
     * List of configuration setting keys, which match the provided key regex.
     * @param keyPattern Regex selector for the required setting names. Null means 'all'.
     * @return List of matching configuration keys.
     * @see getSetting
     */
    fun getSettingKeys(keyPattern: String? = null): Set<String>

    /**
     * Transform given setting to get its value, eventually after encrypting/decrypting it.
     * @param value Config setting value.
     * @param encryption Desired value encryption.
     * @return Transformed value.
     * @see getSetting
     */
    fun transformValue(value: String?, encryption: Encryption? = null): Any?
}
