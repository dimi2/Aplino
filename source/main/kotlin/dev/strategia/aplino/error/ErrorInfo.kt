package dev.strategia.aplino.error

import java.io.Serializable
import java.util.IllegalFormatException

/**
 * Holder for descriptive error information. It contains:
 * - Code. Standardized constant which is easy to search for in documents and internet. Like 'E1105'.
 * - Error details (text) describing the error.
 * - Error details parameters, which fill the placeholders in the details text.
 * - Technical advice to the developer, how to fix the error. This is technical and should not be shown
 *   to the end user.
 * - Context of the exception (supplementary objects, useful for debugging).
 */
open class ErrorInfo: Serializable, Cloneable {
    /** Storage identifier of the record. */
    var id: Serializable? = null
    /** Unique error code. */
    var code: String? = null
    /** Error text to show to the user (may contain placeholders). */
    var details: String?= null
    /** Error text placeholder values. */
    var detailsParams: Array<Any?>? = null
    /** Additional context objects for debugging. */
    var context: Map<String, Any?>? = null
    /** Error ticket (unique error id for the logs). */
    var ticket: String? = null
    /** The object which throws the error. */
    @Transient
    var origin: Any? = null

    constructor(code: String) : this(code, null)

    constructor(code: String?, details: String?, vararg detailsParams: Any?) {
        this.code = code
        this.details = details
        if ((code == null) && (details == null)) {
            throw IllegalArgumentException("Missing mandatory parameter")
        }
        this.detailsParams = arrayOf(*detailsParams)
    }

    /**
     * Get the error details with the parameter placeholders filled in. The [details] template uses
     * [String.format] style placeholders (like `%s`). If the template has no placeholders (or they do
     * not match the parameters), the raw template is returned unchanged.
     * @return The formatted details text, or null if no details are set.
     */
    open fun formattedDetails(): String? {
        val template = details
        val params = detailsParams
        var result = template
        if (template != null && !params.isNullOrEmpty()) {
            try {
                result = String.format(template, *params)
            } catch (_: IllegalFormatException) {
                // The template has no (or invalid) placeholders for the given parameters. Keep it as is.
                result = template
            }
        }
        return result
    }

    /**
     * Create copy of the error holder (without the details parameters, which are instance specific).
     * @return Copy of the error info.
     */
    public override fun clone(): ErrorInfo {
        val clone: ErrorInfo
        try {
            clone = super.clone() as ErrorInfo
            clone.detailsParams = null
            clone.context = null
            clone.origin = null
            return clone
        } catch (e: CloneNotSupportedException) {
            throw IllegalStateException(e)
        }
    }

    override fun toString(): String {
        return "{$code, $details, ticket=$ticket}"
    }
}
