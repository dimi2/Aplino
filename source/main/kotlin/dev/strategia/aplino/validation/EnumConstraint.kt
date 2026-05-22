package dev.strategia.aplino.validation

import dev.strategia.aplino.error.Errors
import java.util.Locale

/**
 * Constraint for enumerations.
 */
open class EnumConstraint : Constraint {
    protected var enumValues: Array<out Enum<*>>
    protected var mandatory = false
    protected var ignoreCase = false

    constructor(enumClass: Class<out Enum<*>>, isMandatory: Boolean = false, ignoreCase: Boolean = false) {
        this.enumValues = enumClass.enumConstants
        this.mandatory = isMandatory
        this.ignoreCase = ignoreCase
    }

    override fun accept(fieldName: String, value: Any?, context: ValidationContext): Any? {
        var ret: Enum<*>? = null
        if (value != null) {
            if (value is Enum<*>) {
                ret = value
            }
            else {
                var v = value.toString()
                if (ignoreCase) {
                    v = convertEnumString(v)
                }
                ret = createEnum(v)
            }
        }
        if ((ret == null) && mandatory) {
            context.addError(ValidationErrors.createError(Errors.FIELD_IS_EMPTY, fieldName, fieldName))
        }
        return ret
    }

    protected open fun createEnum(name: String): Enum<*>? {
        return enumValues.firstOrNull { it.name == name }
    }

    protected open fun convertEnumString(enumStr: String): String {
        return enumStr.uppercase(Locale.getDefault())
    }
}
