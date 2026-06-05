package dev.strategia.aplino.validation

import dev.strategia.aplino.error.Errors

/**
 * Validator for accepting custom classes.
 * The converter function converts the passed value to custom class.
 * The checker function checks if the value is logically valid and returns true/false. There could be called
 * specific checks, which require more complicated coding.
 */
open class CheckedConstraint : Constraint {
    val checker: ValueChecker?
    val converter: ClassConverter?
    val mandatory: Boolean

    constructor(isMandatory: Boolean, converter: ClassConverter? = null, checker: ValueChecker? = null) {
        this.converter = converter
        this.checker = checker
        mandatory = isMandatory
    }

    override fun accept(fieldName: String, value: Any?, context: ValidationContext): Any? {
        var converted: Any? = null
        if (value != null) {
            // Convert the value to the target class (when a converter is configured).
            var v: Any? = value
            if (converter != null) {
                v = convertToClass(value)
            }
            if (v == null) {
                // Conversion failed.
                context.addError(ValidationErrors.createError(Errors.FIELD_WRONG_SYNTAX,
                    fieldName, fieldName))
            }
            else if (checker != null) {
                // Check the (eventually converted) value.
                if (checker.invoke(v)) {
                    converted = v
                }
                else {
                    context.addError(ValidationErrors.createError(Errors.FIELD_WRONG_VALUE,
                        fieldName, value))
                }
            }
            else {
                converted = v
            }
        }
        else {
            if (mandatory) {
                context.addError(ValidationErrors.createError(Errors.FIELD_IS_EMPTY, fieldName, fieldName))
            }
        }
        return converted
    }

    /**
     * Convert provided value to custom field class. This is extension point.
     * @param value - The value to be converted.
     * @return Converted value, or null if the value cannot be converted.
     */
    protected open fun convertToClass(value: Any): Any? {
        val converted = try {
            converter?.invoke(value)
        }
        catch (_: Exception) {
            // Conversion failed. Do not return any result.
        }
        return converted
    }
}

/** Converts a raw value to the target field class, or returns null if it cannot be converted. */
typealias ClassConverter = (Any) -> Any?

/** Checks whether a (possibly converted) value is valid. */
typealias ValueChecker = (Any) -> Boolean
