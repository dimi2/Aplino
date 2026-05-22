package dev.strategia.aplino.config

import java.io.Serializable

/**
 * Hold single setting from application configuration.
 * Contains not only key and value, but also description, encryption flag etc.
 */
open class ConfigSetting(var value: Any?) : Serializable {
    var description: String? = null
    var defaultValue: Any? = null
    var originalValue: Any? = null
    var encryption: Encryption? = null
}
