package dev.strategia.aplino.validation

import dev.strategia.aplino.error.Errors

/**
 * Constraint for objects in collection (List, Set).
 * The collection elements are validated and converted by the provided BaseConstraint.
 */
open class CollectionConstraint : Constraint {
    var valueSeparator = ","
    protected val collectionClass: Class<out MutableCollection<*>>
    protected val itemConstraint: Constraint?
    protected var mandatory = false

    constructor(collectionClass: Class<out MutableCollection<*>>, itemConstraint: Constraint?,
                isMandatory: Boolean? = false) {
        this.collectionClass = collectionClass
        this.itemConstraint = itemConstraint
        if (isMandatory != null) {
            this.mandatory = isMandatory
        }
    }

    override fun accept(fieldName: String, value: Any?, context: ValidationContext): Any? {
        @Suppress("UNCHECKED_CAST")
        val collection = createInstance(collectionClass) as MutableCollection<Any?>
        if (value != null) {
            val values = splitValue(value, valueSeparator)
            if (itemConstraint != null) {
                val errorCount = context.getErrorsCount()
                for (v in values) {
                    val converted = itemConstraint.accept(fieldName, v, context)
                    if (context.getErrorsCount() > errorCount) {
                        // In case of error, do not return any result.
                        collection.clear()
                        break
                    }
                    collection.add(converted)
                } //
            }
            else {
                collection.addAll(values)
            }
        }
        if (collection.isEmpty() && mandatory) {
            context.addError(ValidationErrors.createError(Errors.FIELD_IS_EMPTY, fieldName, fieldName))
        }
        return collection
    }

    protected open fun splitValue(value: Any, separator: String): Collection<*> {
        val list = mutableListOf<Any?>()
        if (value is Collection<*>) {
            list.addAll(value)
        }
        else if (value is CharSequence) {
            value.split(separator).forEach { e -> list.add(e.trim()) }
        }
        return list
    }

    protected open fun createInstance(clazz: Class<*>): Any {
        return clazz.getDeclaredConstructor().newInstance()
    }
}
