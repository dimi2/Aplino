package dev.strategia.aplino.validation

import dev.strategia.aplino.error.Errors
import java.util.TreeMap

/**
 * Standardized validation errors. Practically every application defines them. Applications can extend this
 * class to add own validation errors.
 */
open class ValidationErrors {
    companion object {
        protected var errorMap: MutableMap<Int, String> = TreeMap()

        init {
            errorMap[Errors.FIELD_IS_EMPTY] = "The field cannot be empty (%s)"
            errorMap[Errors.FIELD_WRONG_SIZE] = "Wrong length (min %s : max %s)"
            errorMap[Errors.FIELD_WRONG_RANGE] = "Wrong range (min %s : max %s)"
            errorMap[Errors.FIELD_WRONG_DATE] = "Wrong date (min %s : max %s)"
            errorMap[Errors.FIELD_WRONG_TIME] = "Wrong time (min %s : max %s)"
            errorMap[Errors.FIELD_WRONG_SYNTAX] = "Wrong syntax (%s)"
            errorMap[Errors.FIELD_WRONG_VALUE] = "Wrong value (%s)"
            errorMap[Errors.FIELD_DUPLICATE_VALUE] = "Value already in use (%s)"
            errorMap[Errors.FIELD_UNKNOWN] = "Unknown field (%s)"
            errorMap[Errors.OBJECT_IS_INVALID] = "Object is invalid (%s)"
            errorMap[Errors.INPUT_IS_INVALID] = "Input is invalid (%s)"
        }

        /**
         * Get the error message, associated with the specified error code.
         * @param code Error code.
         * @param params Message parameters.
         * @return Error message.
         */
        fun getErrorMessage(code: Int, vararg params: Any?): String? {
            var message: String? = errorMap[code]
            if (message != null) {
                if (params.isNotEmpty()) {
                    message = String.format(message, *params)
                }
            }
            return message
        }

        /**
         * Create validation error for specified error code.
         * @param code Error code.
         * @param field Name of the wrong field.
         * @param params Additional parameters for the error message placeholders.
         * @return Validation error object.
         */
        fun createError(code: Int, field: String?, vararg params: Any?): ValidationError {
            return ValidationError(code, getErrorMessage(code, *params)!!, field)
        }

    }
}
