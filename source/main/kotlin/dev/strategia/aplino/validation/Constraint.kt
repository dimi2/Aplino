package dev.strategia.aplino.validation

/**
 * Interface for object field validation constraints (later checked by [Validator]).
 * This validation approach does not require annotations,
 * and does not create lots of temporary objects (to be garbage collected by the JVM later).
 * It is lightweight alternative of [Jakarta Validation](https://beanvalidation.org/).
 */
interface Constraint {

    /**
     * Validate and accept specified field value.
     * @param fieldName The field name.
     * @param value The field value to be validated.
     * @param context Validation context. The errors will be added there.
     * @return Validated (and potentially converted to different class) value.
     */
    fun accept(fieldName: String, value: Any?, context: ValidationContext): Any?

}
