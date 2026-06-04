package dev.strategia.aplino.validation

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * Object field validator. It validates the fields of the provided object.
 */
open class Validator() {
    companion object {
        const val EMPTY_STRING = ""
    }
    /** Class to which belong the fields.  */
    var targetClass: Class<*>? = null
    var targetSetters: Map<String, KMutableProperty<*>>? = null
    /** Object field constraints: fieldName = Constraints.  */
    protected var fieldConstraints = mutableMapOf<String, MutableSet<ConstraintHolder>>()

    constructor(targetClass: Class<*>): this() {
        this.targetClass = targetClass
        val setters = mutableMapOf<String, KMutableProperty<*>>()
        if (!targetClass.interfaces.contains(MutableMap::class.java)) {
            val properties = targetClass.kotlin.memberProperties
            for (property in properties) {
                if (property is KMutableProperty<*>) {
                    setters[property.name] = property
                }
            }
        }
        this.targetSetters = setters
    }

    /**
     * Add field constraints.
     * @param forField Name of the field for which are the constraints.
     * @param constraints Field validation constraints.
     */
    open fun constrain(forField: String, vararg constraints: Constraint) {
        for (constraint in constraints) {
            val fcList = getConstraints(forField)
            fcList.add(ConstraintHolder(constraint, null))
        } //
    }

    /**
     * Add field constraints.
     * @param forField Name of the field for which are the constraints.
     * @param targetField The field to which the constraint result should be assigned. Use it if the
     *        converted value should be assigned to other object field.
     * @param constraints Field validation constraints.
     */
    open fun constrain(forField: String, targetField: String, vararg constraints: Constraint) {
        for (constraint in constraints) {
            val fcList = getConstraints(forField)
            fcList.add(ConstraintHolder(constraint, targetField))
        } //
    }

    /**
     * Get the constraints assigned to specified field.
     * @param fieldName Field name.
     * @return Constraints for this field.
     */
    open fun getConstraints(fieldName: String): MutableSet<ConstraintHolder> {
        var ret: MutableSet<ConstraintHolder>? = fieldConstraints[fieldName]
        if (ret == null) {
            ret = mutableSetOf()
            fieldConstraints[fieldName] = ret
        }
        return ret
    }

    /**
     * Validate specified object.
     * @param fieldValues Fields values provider (method reference).
     * @param context Validation context (where to collect the errors).
     * @return Object of the target class, with validated field values.
     */
    open fun validateObject(fieldValues: (String) -> Any?, context: ValidationContext): Any {
        if (targetClass == null) {
            throw IllegalStateException("Target class is not provided")
        }

        val obj = createObjectInstance(targetClass!!)
        for (fieldConstraint in fieldConstraints) {
            val fieldName = fieldConstraint.key
            var fieldValue: Any? = fieldValues.invoke(fieldName)
            if (EMPTY_STRING == fieldValue) {
                fieldValue = null // Treat empty strings as null.
            }
            for (ch in fieldConstraint.value) {
                val constraint = ch.constraint
                fieldValue = constraint.accept(fieldName, fieldValue, context)
                if (fieldValue != null) {
                    val property = targetSetters!![ch.targetField ?: fieldName]
                    if (property != null) {
                        // Object with setters and getters.
                        try {
                            property.setter.call(obj, fieldValue)
                        } catch (e: Exception) {
                            val message = "Cannot set ${obj::class.java}.${fieldName}" +
                                " with value '$fieldValue' (${fieldValue::class.java})"
                            throw IllegalArgumentException(message, e)
                        }
                    }
                    else {
                        // Generic map object.
                        if (targetClass!!.interfaces.contains(MutableMap::class.java) ||
                            targetClass!!.interfaces.contains(java.util.SequencedMap::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            val map = obj as MutableMap<Any, Any?>
                            map[ch.targetField ?: fieldName] = fieldValue
                        }
                    }
                }
            } //
        } //
        return obj
    }

    /**
     * Validate specified field.
     * @param fieldName Field name.
     * @param value Field value (to be validated).
     * @param context Validation context (where to collect the errors).
     * @return Validated (and possibly converted) field value.
     */
    open fun validateField(fieldName: String, value: Any?, context: ValidationContext): Any? {
        var converted: Any? = null
        val constraints = getConstraints(fieldName)
        for (ch in constraints) {
            val constraint = ch.constraint
            val initialErrorsCount = context.getErrorsCount()
            converted = constraint.accept(fieldName, value, context)
            if (context.getErrorsCount() > initialErrorsCount) {
                break
            }
        } //
        return converted
    }

    protected open fun createObjectInstance(clazz: Class<*>): Any {
        return clazz.getDeclaredConstructor().newInstance()
    }

    class ConstraintHolder(val constraint: Constraint, val targetField: String? = null)
}
