package dev.strategia.aplino.validation

import dev.strategia.aplino.error.Errors

/**
 * Validator for accepting/converting custom values. The checker function passed to the validator checks
 * if the value is valid and returns true/false. The converter function converts the checked value to class.
 */
open class CheckedConstraint : Constraint {
    val checker: ValueChecker?
    val converter: ClassConverter?
    val mandatory: Boolean

    constructor(isMandatory: Boolean, checker: ValueChecker? = null, classConverter: ClassConverter? = null) {
        this.checker = checker
        mandatory = isMandatory
        converter = classConverter
    }

    override fun accept(fieldName: String, value: Any?, context: ValidationContext): Any? {
        var converted: Any? = null
        if (value != null) {
            var v = value
            if (converter != null) {
                // Convert the value to the target class.
                v = convertToClass(value)
                if (v == null) {
                    context.addError(ValidationErrors.createError(Errors.FIELD_WRONG_SYNTAX,
                        fieldName, fieldName))
                    return null
                }
            }

            if (checker != null) {
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
            // Do not return any result.
        }
        return converted
    }
}

typealias ValueChecker = (Any?) -> Boolean
typealias ClassConverter = (Any) -> Any?
