package dev.strategia.aplino.validation

/**
 * Exception for validation errors (form data or object submitted by the user).
 * The application cannot recover from such error.
 */
open class ValidationException : RuntimeException {
    var errors = mutableListOf<ValidationError>()
    var forObject: Any? = null

    constructor(message: String) : super(message)

    /**
     * Constructor with cause.
     * @param message Error description message.
     * @param cause The exception which caused the error.
     */
    constructor(message: String, cause: Throwable) : super(message, cause)

    /**
     * Add a validation error.
     * @param error Validation error to be added.
     * @return True if the error adding changed the errors list.
     */
    fun addError(error: ValidationError): Boolean {
        return errors.add(error)
    }

    override fun toString(): String {
        return "$message $errors"
    }

}
