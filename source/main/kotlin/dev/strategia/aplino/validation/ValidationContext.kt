package dev.strategia.aplino.validation

import java.io.Serializable

/**
 * Validation context holder. Keeps validation related state until all the checks are finished.
 */
open class ValidationContext() {
    /** List of detected errors.  */
    protected var errors = mutableSetOf<ValidationError>()
    /** The locale of the current validation (two-letter code, lowercase).  */
    var locale: String? = null
    /** The currently validated object.  */
    var currentObject: Serializable? = null
    /** Flag to throw exception when validation error is added.  */
    protected var exceptionOnError: Boolean = false

    constructor(ctx: ValidationContext) : this() {
        copy(ctx)
    }

    /**
     * Add detected error to the errors list. It the list already contains this error, nothing is added.
     * @param error The error.
     * @return True if the error list was actually modified.
     * @throws ValidationException if the flag for 'exception on error' is on.
     */
    @Throws(ValidationException::class)
    open fun addError(error: ValidationError?): Boolean {
        var ret = false
        if (error != null) {
            ret = errors.add(error)
            if (exceptionOnError) {
                raiseException(error)
            }
        }
        return ret
    }

    /**
     * Add detected errors to the errors list.
     * @param errorList The errors to add.
     * @return True if the error list was actually modified.
     * @throws ValidationException if the flag for 'exception on error' is on.
     */
    open fun addErrors(errorList: Collection<ValidationError>): Boolean {
        var ret = false
        for (error in errorList) {
            ret = ret or addError(error)
        } //
        return ret
    }

    /**
     * Remove an error from the errors list (we may have error recovery).
     * @param error The error to be removed.
     * @return True if the error list was actually modified.
     */
    open fun removeError(error: ValidationError): Boolean {
        return errors.remove(error)
    }

    /**
     * Clear the errors list.
     */
    open fun clearErrors() {
        errors.clear()
    }

    /**
     * Check of the context contains errors.
     * @return True if the error list is not empty.
     */
    open fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    /**
     * Get the errors contained in the context.
     * @return Errors list.
     */
    open fun getErrors(): List<ValidationError> {
        return errors.toList()
    }

    /**
     * Get the errors count in the context.
     * @return Number of errors.
     */
    open fun getErrorsCount(): Int {
        return errors.size
    }

    /**
     * Set a flag to throw validation exception if an error is added to the context.
     * This allows to support two error handling strategies:
     * - Stop on first detected error. Usually we want this, because it stops next operations.
     * - Collect all errors and then stop. This is the behavior for user input handling
     * (all issues from the form are checked at once to minimize re-submit attempts)
     * @param throwException True to throw exception.
     */
    open fun throwExceptionOnError(throwException: Boolean) {
        this.exceptionOnError = throwException
    }

    /**
     * Reset the context state. This will clear all internal values and the context could be reused,
     * without creating new instance.
     */
    open fun reset() {
        clearErrors()
        currentObject = null
        locale = null
    }

    /**
     * Copy the state of other context.
     * @param ctx Other context to copy from.
     */
    open fun copy(ctx: ValidationContext?) {
        if (ctx != null) {
            currentObject = ctx.currentObject
            addErrors(ctx.getErrors())
            locale = ctx.locale
        }
    }

    /**
     * Raise a validation exception.
     * @param forError Last validation error. If null, the exception will include all validation errors
     * * detected so far.
     */
    @Throws(ValidationException::class)
    open fun raiseException(forError: ValidationError?) {
        val exception = ValidationException("Validation failed.")
        exception.forObject = currentObject
        val errors = if (forError == null) getErrors() else listOf(forError)
        exception.errors = errors as MutableList<ValidationError>
        throw exception
    }
}
