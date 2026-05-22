package dev.strategia.aplino.error

/**
 * Standard error codes. They allow to classify errors and refer them by code.
 * Applications extend this with their custom error codes.
 */
open class Errors {
    companion object {
        // Validation errors.
        const val FIELD_IS_EMPTY = 1001
        const val FIELD_WRONG_SIZE = 1002
        const val FIELD_WRONG_RANGE = 1003
        const val FIELD_WRONG_DATE = 1004
        const val FIELD_WRONG_TIME = 1005
        const val FIELD_WRONG_SYNTAX = 1006
        const val FIELD_WRONG_VALUE = 1007
        const val FIELD_DUPLICATE_VALUE = 1008
        const val FIELD_UNKNOWN = 1009
        const val OBJECT_IS_INVALID = 1010
        const val INPUT_IS_INVALID = 1011

        // Execution errors.
        const val EVENT_HANDLING_FAILURE = 2001
    }
}
