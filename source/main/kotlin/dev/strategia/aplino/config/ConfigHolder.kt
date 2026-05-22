package dev.strategia.aplino.config

import java.io.Serializable

/**
 * Holder for persisted configuration data. It allows keeping config settings independently of the
 * format used to store them.
 */
open class ConfigHolder: Serializable {
    var comment: String? = null
    var format = 0
    var settings = LinkedHashMap<String, ConfigSetting?>()
}
