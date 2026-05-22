package dev.strategia.aplino.util

import java.io.Serializable

/**
 * Holder to pass value as method parameter. Thus, the method could modify it (allows parameter mutation).
 */
open class ValueHolder<T>() : Serializable {
    /** Wrapped value.  */
    var value: T? = null

    /**
     * Constructor with specified value.
     * @param value The value to wrap.
     */
    constructor(value: T): this() {
        this.value = value
    }

    /**
     * Human-readable string representation of the object.
     * @return The object as string.
     */
    override fun toString(): String {
        return "($value)"
    }
}
