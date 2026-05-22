package dev.strategia.aplino.text

import java.io.Serializable
import java.util.Formatter

/**
 * Represents a localization-enabled text entry. Usually sentence which is loaded from external source.
 * May contain placeholders for variables in printf format (like "%s").
 */
open class TextEntry() : Serializable {
    lateinit var key: String
    lateinit var text: String
    var format: Formatter? = null

    constructor(key: String, text: String) : this() {
        this.key = key
        this.text = text
    }


}
