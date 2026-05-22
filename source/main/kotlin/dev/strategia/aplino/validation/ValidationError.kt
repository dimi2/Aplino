package dev.strategia.aplino.validation

import java.io.Serializable

/**
 * Represents validation error.
 * The validation error is different from the application error, because:
 * - It is made by the user. No need go to the logs.
 * - It is fixed by the user. No need to be investigated.
 * - It is related to specific form field (or object property).
 * - The application cannot recover automatically from such error.
 */
open class ValidationError(val errorCode: Int?, val errorMessage: String,
                           val fieldName: String?) : Serializable {

    override fun toString(): String {
        return "$fieldName: $errorMessage"
    }
}
