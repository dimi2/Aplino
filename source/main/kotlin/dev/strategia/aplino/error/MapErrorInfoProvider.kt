package dev.strategia.aplino.error

import java.util.TreeMap

/**
 * Basic implementation of error info provider. It actually does not load error information but serves it
 * from in-memory map.
 */
open class MapErrorInfoProvider : ErrorInfoProvider {
    val errors = TreeMap<String, ErrorInfo?>()

    override fun load(errorCode: String): ErrorInfo? {
        return errors[errorCode]
    }

    override fun store(errorInfo: ErrorInfo) {
    }
}
