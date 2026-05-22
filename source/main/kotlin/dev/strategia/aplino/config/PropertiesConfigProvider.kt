package dev.strategia.aplino.config

import dev.strategia.aplino.util.PropertiesFileParser
import dev.strategia.aplino.util.PropertiesFileWriter

/**
 * Configuration data provider working with extended PROPERTIES file format.
 */
open class PropertiesConfigProvider : ConfigProvider {
    protected var keyComment = "comment"
    protected var keyFormat = "format"

    override fun load(configLocation: String): ConfigHolder {
        val config = ConfigHolder()
        config.format = 1
        config.settings = LinkedHashMap()
        val params = PropertiesFileParser().readFile(configLocation)
        for (param in params) {
            when (param.key) {
                keyComment -> config.comment = param.value as String
                keyFormat -> config.format =  (param.value as String).toInt()
                else -> config.settings[param.key] = ConfigSetting(param.value)
            }
        } //
        return config
    }

    override fun store(configLocation: String, config: ConfigHolder) {
        val writer = PropertiesFileWriter(configLocation)
        writer.use { w ->
            w.writeProperty(keyComment, config.comment)
            w.writeProperty(keyFormat, config.format)
            w.writeFile(config.settings)
        }
    }
}
