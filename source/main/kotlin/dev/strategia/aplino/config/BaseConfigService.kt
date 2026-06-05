package dev.strategia.aplino.config

import dev.strategia.aplino.error.DataStructureException
import dev.strategia.aplino.log.LogService
import dev.strategia.aplino.security.DataEncryptor
import java.io.File
import java.security.Key
import java.util.Base64
import java.util.TreeMap
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Base config service implementation.
 * The configuration sequence is:
 * 1. If config files are specified, their settings are extracted. If some setting is duplicated in
 * different files, the one mentioned first in the config files list will be used.
 * 2. The extracted settings are merged with environment variables (having predefined name prefix).
 * If there are duplicated setting names, the environment values will be used.
 * 3. The result settings merge with the command line parameters. If there are duplicated setting names,
 * the parameter values will be used. So, the command line arguments have the highest priority.
 *
 * Note: the setting names must not contain characters, forbidden for environment name variables.
 */
open class BaseConfigService : ConfigService {
    companion object {
        /** Maximum number of variable replacements per value, to guard against cyclic references. */
        protected const val MAX_VARIABLE_REPLACEMENTS = 100
    }

    var logService: LogService? = null
    val encryptor: DataEncryptor?
    var encryptedSuffix: String? = null
    var fileValuePrefix = "/run/secrets/"
    /**
     * Prefix for environment variables that configure the application. When set, environment variables
     * whose name starts with this prefix override (and may add) configuration settings, with the prefix
     * stripped from the name.
     */
    var envPrefix: String? = null
    protected val config = ConfigHolder()
    protected var configKey: Key? = null
    /** Variable search pattern.  */
    protected var variablePattern: Pattern = Pattern.compile("[#$]\\{([^}]+)}")

    constructor(homeDir: String, configFiles: String?, logService: LogService? = null,
                encryptor: DataEncryptor? = null, parameters: Map<String, String?>? = null,
                providers: Map<String, ConfigProvider>? = null) {
        this.logService = logService
        this.encryptor = encryptor
        if (!configFiles.isNullOrEmpty()) {
            val confList = configFiles.trim().split(',').reversed()
            configure(homeDir, confList, parameters, providers)
        }
    }

    /**
     * Set the encryption key file, to use for encrypted config settings.
     * @param keyContent The encryption key content.
     */
    fun setEncryptionKey(keyContent: String?) {
        if (keyContent != null) {
            configKey = createEncryptionKey(keyContent.toCharArray())
        }
        else {
            configKey = null
        }
    }

    fun setSettings(settings: Map<String, ConfigSetting?>) {
        for (entry in settings.entries) {
            setSetting(entry.key, entry.value)
        } //
    }

    override fun setValue(key: String, value: Any?) {
        val setting = ConfigSetting(value)
        if (encryptedSuffix != null && key.endsWith(encryptedSuffix!!)) {
            setting.encryption = Encryption.DO
        }
        setSetting(key, setting)
    }

    override fun getValue(key: String, defaultValue: Any?): Any? {
        return getSettingValue(key) ?: defaultValue
    }

    override fun getValues(keyPattern: String?): Map<String, Any?> {
        val ret = TreeMap<String, Any?>()
        val regex = keyPattern?.toRegex()
        for (key in config.settings.keys) {
            if (regex == null || key.matches(regex)) {
                ret[key] = getSettingValue(key)
            }
        } //
        return ret
    }

    override fun getValueMandatory(key: String): Any {
        val value = getValue(key)
        return value ?: throw IllegalArgumentException("Mandatory configuration value '$key' is missing")
    }

    override fun setSetting(key: String, setting: ConfigSetting?) {
        val encryption = setting?.encryption
        if (encryption != null) {
            val value = setting.value.toString()
            setting.value = transformValue(value, encryption)
            when (encryption) {
                Encryption.DO -> {
                    setting.encryption = Encryption.DONE
                }
                Encryption.UNDO -> {
                    setting.encryption = Encryption.DONE
                }
                else -> {}
            }
        }
        config.settings[key] = setting
    }

    override fun getSetting(key: String): ConfigSetting? {
        var ret: ConfigSetting? = null
        if (config.settings.containsKey(key)) {
            ret = config.settings[key]
        }
        return ret
    }

    override fun getSettingKeys(keyPattern: String?): Set<String> {
        val ret = LinkedHashSet<String>()
        val regex = keyPattern?.toRegex()
        for (key in config.settings.keys) {
            if (regex == null || key.matches(regex)) {
                ret.add(key)
            }
        } //
        return ret
    }

    override fun transformValue(value: String?, encryption: Encryption?): Any? {
        var res = value
        if (res != null) {
            val cryptoKey = configKey
            if (cryptoKey != null) {
                when (encryption) {
                    Encryption.DO -> {
                        res = encryptSettingValue(res, cryptoKey)
                    }
                    Encryption.UNDO -> {
                        res = decryptSettingValue(res, cryptoKey)
                    }
                    else -> {}
                }
            }
        }
        return res
    }

    protected open fun configure(homeDir: String, configFiles: List<String>,
                         parameters: Map<String, String?>?, providers: Map<String, ConfigProvider>? = null) {
        val logger = logService?.getLogger(this)
        val configHolder = ConfigHolder()
        for (configFile in configFiles) {
            if (configFile.isNotEmpty()) {
                // Determine the correct file name.
                var cf = File(configFile)
                if (!cf.isAbsolute) {
                    cf = File(homeDir + File.separator + configFile)
                }

                // Check config file existence.
                if (!cf.isFile) {
                    logger?.warn("Missing config file: {}", cf)
                    continue
                }

                // Prepare the config loader.
                val configLoader = getConfigProvider(cf, providers)
                if (configLoader != null) {
                    // Load the configuration.
                    logger?.debug("Loading config from: {}", cf)
                    val conf = configLoader.load(cf.path)
                    // Replace configuration placeholders.
                    processConfig(conf)
                    // Add the settings to the config.
                    for (setting in conf.settings) {
                        configHolder.settings[setting.key] = setting.value
                    } //
                }
                else {
                    logger?.debug("Unsupported config format for: {}", cf)
                    continue
                }
            }
        } //

        mergeConfigKeys(configHolder, parameters)
        config.settings.putAll(configHolder.settings)
    }

    protected open fun getSettingValue(key: String?): Any? {
        var ret: Any? = null
        if (key != null) {
            val setting = config.settings[key]
            if (setting != null) {
                ret = getSettingValue(key, setting)
            }
        }
        return ret
    }

    protected open fun getSettingValue(key: String, setting: ConfigSetting): Any? {
        var ret = setting.value
        if (ret != null) {
            if ((ret is String) && (ret.startsWith(fileValuePrefix))) {
                // This is cloud secret, mounted as RAM file, which contains the real value.
                val fv = File(ret)
                if (fv.isFile() && fv.canRead()) {
                    ret = fv.readText()
                }
            }

            if (configKey != null) {
                try {
                    if (setting.encryption != null) {
                        // Decrypt settings which were encrypted.
                        if ((Encryption.DONE == setting.encryption) ||
                            (Encryption.UNDO == setting.encryption)) {
                            ret = decryptSettingValue(ret, configKey!!)
                        }
                    }
                    else if (encryptedSuffix != null && key.endsWith(encryptedSuffix!!)) {
                        // Decrypt settings which has encrypted suffix in their key.
                        ret = decryptSettingValue(ret, configKey!!)
                    }
                }
                catch (e: DataStructureException) {
                    throw DataStructureException("Failed to decrypt config key: $key", e)
                }
            }
        }

        return ret
    }

    /**
     * Process the loaded configuration. It replaces the variables in the values.
     * @param config Configuration settings.
     */
    protected open fun processConfig(config: ConfigHolder) {
        val variables = collectVariables()
        for (entry in config.settings.entries) {
            val setting = entry.value
            if (setting != null) {
                val value = setting.value ?: setting.defaultValue
                if (value is String) {
                    // Replace the variables in the config values.
                    val newValue = replaceVariables(value, variables)
                    if (newValue != value) {
                        setting.originalValue = value
                        setting.value = newValue
                    }
                }
            }
        } //
    }

    /**
     * Replace string value variables defined as `${varName} (or #{varName})` with real values.
     * @param value String value to be replaced.
     * @param variables A map with the real values to use for the replacement.
     * @return Resource value with replaced variables. If specified variable does not exist in
     * variables map, it remains as is.
     */
    protected open fun replaceVariables(value: String, variables: Map<String, Any?>): String? {
        val buf = StringBuffer(value)
        val m: Matcher = variablePattern.matcher(buf)
        var replacements = 0
        while (m.find()) {
            val v = variables[m.group(1)]
            if (v != null) {
                buf.replace(m.start(), m.end(), v.toString())
                if (++replacements > MAX_VARIABLE_REPLACEMENTS) {
                    // Guard against cyclic variable references, which would otherwise loop forever.
                    val logger = logService?.getLogger(this)
                    logger?.warn("Aborting variable replacement after {} substitutions; the value " +
                        "likely contains a cyclic reference: {}", MAX_VARIABLE_REPLACEMENTS, value)
                    break
                }
                // Re-scan from the start, so variables introduced by the replacement are also resolved.
                m.reset()
            }
        } //
        return buf.toString()
    }

    protected open fun collectVariables(): MutableMap<String, Any?> {
        val variables = mutableMapOf<String, Any?>()
        @Suppress("UNCHECKED_CAST")
        variables.putAll(System.getProperties() as Map<String, Any?>)
        return variables
    }

    protected open fun createEncryptionKey(keyContent: CharArray): Key {
        if (encryptor == null) {
            throw IllegalStateException("Encryption service not configured")
        }
        return encryptor.createSecretKey(keyContent)
    }

    protected open fun decryptSettingValue(value: Any, cryptoKey: Key): String {
        var v = Base64.getDecoder().decode(value.toString())
        if (encryptor != null) {
            v = encryptor.decrypt(v, cryptoKey)
        }
        return String(v)
    }

    protected open fun encryptSettingValue(value: Any, cryptoKey: Key): String {
        val result: String
        if (encryptor != null) {
            val v = encryptor.encrypt(value.toString().toByteArray(), cryptoKey)
            result = Base64.getEncoder().encodeToString(v)
        }
        else {
            result = value.toString()
        }
        return result
    }

    protected open fun getConfigProvider(confFile: File,
                                         providers: Map<String, ConfigProvider>?): ConfigProvider? {
        var res: ConfigProvider? = null
        val fileType = confFile.extension
        if (providers != null) {
            res = providers[fileType]
        }
        if (res == null) {
            res = PropertiesConfigProvider()
        }
        return res
    }

    protected open fun mergeConfigKeys(conf: ConfigHolder, parameters: Map<String, String?>?) {
        // Merge with the environment parameters (which have priority).
        val envSettings = System.getenv()
        val prefix = envPrefix
        if (prefix.isNullOrEmpty()) {
            // No prefix configured: environment variables only override existing settings.
            val newSettings = mutableMapOf<String, ConfigSetting?>()
            for (entry in conf.settings) {
                val key = entry.key
                if (envSettings.containsKey(key)) {
                    val setting = entry.value
                    val newValue = envSettings[key]
                    if (newValue != setting?.value) {
                        if (setting != null) {
                            setting.value = newValue
                        }
                        else {
                            newSettings[key] = ConfigSetting(newValue)
                        }
                    }
                }
            } //
            conf.settings += newSettings
        }
        else {
            // Prefixed environment variables override existing settings and may add new ones.
            for ((envKey, envValue) in envSettings) {
                if (envKey.startsWith(prefix) && envKey.length > prefix.length) {
                    val key = envKey.substring(prefix.length)
                    val setting = conf.settings[key]
                    if (setting != null) {
                        setting.value = envValue
                    }
                    else {
                        conf.settings[key] = ConfigSetting(envValue)
                    }
                }
            } //
        }

        // Merge with the command line parameters (which have priority).
        if (!parameters.isNullOrEmpty()) {
            for (entry in parameters.entries) {
                val key = entry.key
                val setting = conf.settings[key]
                val oldValue = setting?.value
                val newValue = parameters[key]
                if (newValue != oldValue) {
                    if (setting != null) {
                        setting.value = newValue
                    }
                    else {
                        conf.settings[key] = ConfigSetting(newValue)
                    }
                }
            } //
        }
    }
}
