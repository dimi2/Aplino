package dev.strategia.aplino.validation

import dev.strategia.aplino.error.Errors
import dev.strategia.aplino.validation.ValidationErrors.Companion.createError
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date
import java.util.regex.Pattern

/**
 * Standard validation constraints, combined to reduce the creation of internal objects.
 * It handles the most frequently used constraints:
 * - Mandatory field.
 * - Restricted string length.
 * - Restricted numeric range.
 * - Restricted date range.
 * - Restricted time range.
 * - Regular expression match.
 */
open class BaseConstraint : Constraint {
    companion object {
        protected var timeZone: ZoneId = ZoneId.systemDefault()
        protected var dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE!!
        protected var timeFormatter = DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toFormatter()
            .withZone(timeZone)!!
    }

    protected var fieldClass: Class<*>
    protected var mandatory: Boolean? = null
    protected var minLength: Int? = null
    protected var maxLength: Int? = null
    protected var min: Number? = null
    protected var max: Number? = null
    protected var regex: Pattern? = null
    protected var minDate: Date? = null
    protected var maxDate: Date? = null
    protected var minTime: Long? = null
    protected var maxTime: Long? = null
    protected var minInstant: Instant? = null
    protected var maxInstant: Instant? = null
    protected var dFormatter: DateTimeFormatter = dateFormatter
    protected var tFormatter: DateTimeFormatter = timeFormatter

    constructor(toClass: Class<*>) {
        this.fieldClass = toClass
    }

    /**
     * Add mandatory constraint.
     * @param yes True to require mandatory value.
     * @return This object (for chained calls).
     */
    open fun mandatory(yes: Boolean): BaseConstraint {
        mandatory = yes
        return this
    }

    /**
     * Add length constraint.
     * @param min Minimum allowed value length.
     * @param max Maximum allowed value length.
     * @return This object (for chained calls).
     */
    open fun length(min: Int?, max: Int?): BaseConstraint {
        minLength(min)
        maxLength(max)
        return this
    }

    /**
     * Add minimum length constraint.
     * @param length Minimum allowed value length.
     * @return This object (for chained calls).
     */
    open fun minLength(length: Int?): BaseConstraint {
        minLength = length
        return this
    }

    /**
     * Add maximum length constraint.
     * @param length Maximum allowed value length.
     * @return This object (for chained calls).
     */
    open fun maxLength(length: Int?): BaseConstraint {
        maxLength = length
        return this
    }

    /**
     * Add number range constraint.
     * @param min Minimum allowed value.
     * @param max Maximum allowed value.
     * @return This object (for chained calls).
     */
    open fun range(min: Number, max: Number): BaseConstraint {
        min(min)
        max(max)
        return this
    }

    /**
     * Add minimum number constraint.
     * @param minimum Minimum allowed value.
     * @return This object (for chained calls).
     */
    open fun min(minimum: Number): BaseConstraint {
        min = minimum
        return this
    }

    /**
     * Add maximum number constraint.
     * @param maximum Maximum allowed value.
     * @return This object (for chained calls).
     */
    open fun max(maximum: Number): BaseConstraint {
        max = maximum
        return this
    }

    /**
     * Add regular expression constraint.
     * @param pattern Regex describing required value syntax.
     * @return This object (for chained calls).
     */
    open fun regex(pattern: String): BaseConstraint {
        regex = Pattern.compile(pattern)
        return this
    }

    /**
     * Add minimum date constraint.
     * @param minimum Minimum allowed date.
     * @return This object (for chained calls).
     */
    open fun min(minimum: Date): BaseConstraint {
        minDate = minimum
        return this
    }

    /**
     * Add maximum date constraint.
     * @param maximum Maximum allowed date.
     * @return This object (for chained calls).
     */
    open fun max(maximum: Date): BaseConstraint {
        maxDate = maximum
        return this
    }

    /**
     * Add minimum datetime constraint.
     * @param minimum Minimum allowed time.
     * @return This object (for chained calls).
     */
    open fun min(minimum: OffsetDateTime): BaseConstraint {
        minInstant = minimum.toInstant()
        return this
    }

    /**
     * Add maximum datetime constraint.
     * @param maximum Maximum allowed time.
     * @return This object (for chained calls).
     */
    open fun max(maximum: OffsetDateTime): BaseConstraint {
        maxInstant = maximum.toInstant()
        return this
    }
    /**
     * Add minimum time constraint.
     * @param minimum Minimum allowed time (in milliseconds).
     * @return This object (for chained calls).
     */
    open fun minTime(minimum: Long): BaseConstraint {
        minTime = minimum
        return this
    }

    /**
     * Add maximum time constraint.
     * @param maximum Maximum allowed time.
     * @return This object (for chained calls).
     */
    open fun maxTime(maximum: Instant): BaseConstraint {
        maxInstant = maximum
        return this
    }

    /**
     * Add minimum time constraint.
     * @param minimum Minimum allowed time.
     * @return This object (for chained calls).
     */
    open fun minTime(minimum: Instant): BaseConstraint {
        minInstant = minimum
        return this
    }

    /**
     * Add maximum time constraint.
     * @param maximum Maximum allowed time (in milliseconds).
     * @return This object (for chained calls).
     */
    open fun maxTime(maximum: Long): BaseConstraint {
        maxTime = maximum
        return this
    }

    /**
     * Set custom date format. The default format is ISO date.
     * @param pattern Date format pattern.
     * @return this object (for chained calls).
     */
    open fun dateFormat(pattern: String): BaseConstraint {
        dateFormat(DateTimeFormatter.ofPattern(pattern).withZone(timeZone))
        return this
    }

    /**
     * Set custom date format. The default format is ISO date.
     * @param formatter Date formatter to use.
     * @return this object (for chained calls).
     */
    open fun dateFormat(formatter: DateTimeFormatter): BaseConstraint {
        dFormatter = formatter
        return this
    }

    /**
     * Set custom time format. The default format is ISO time.
     * @param pattern Time format pattern.
     * @return this object (for chained calls).
     */
    open fun timeFormat(pattern: String): BaseConstraint {
        timeFormat(DateTimeFormatter.ofPattern(pattern).withZone(timeZone))
        return this
    }

    /**
     * Set custom time format. The default format is ISO time.
     * @param formatter Time formatter to use.
     * @return this object (for chained calls).
     */
    open fun timeFormat(formatter: DateTimeFormatter): BaseConstraint {
        tFormatter = formatter
        return this
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod", "ReturnCount")
    override fun accept(fieldName: String, value: Any?, context: ValidationContext): Any? {
        // Mandatory?
        var isValid = true
        if (value == null) {
            if (mandatory == true) {
                isValid = false
            }
            else {
                return null
            }
        }
        else {
            if ((value is CharSequence) && value.isBlank()) {
                isValid = false
            }
        }
        if (!isValid) {
            context.addError(createError(Errors.FIELD_IS_EMPTY, fieldName, fieldName))
            return null
        }

        // Length?
        if (minLength != null || maxLength != null) {
            isValid = checkLength(fieldName, value.toString())
            if (!isValid) {
                context.addError(createError(Errors.FIELD_WRONG_SIZE, fieldName,
                    minLength ?: "*", maxLength ?: "*"))
                return null
            }
        }

        // Convert the value to the target class.
        val converted = toTargetClass(value!!, fieldClass)
        if (converted == null) {
            context.addError(createError(Errors.FIELD_WRONG_SYNTAX, fieldName, fieldName))
            return null
        }

        // Range?
        if (min != null || max != null) {
            if (converted is Number) {
                isValid = checkRange(fieldName, converted)
            }
            else {
                isValid = false
            }
            if (!isValid) {
                context.addError(createError(Errors.FIELD_WRONG_RANGE, fieldName,
                    min ?: "*", max ?: "*"))
                return null
            }
        }

        // Regex.
        if (regex != null) {
            isValid = checkRegex(fieldName, converted.toString())
            if (!isValid) {
                context.addError(createError(Errors.FIELD_WRONG_SYNTAX, fieldName, value))
                return null
            }
        }

        // Date.
        if (minDate != null || maxDate != null) {
            if (converted is Date) {
                isValid = checkDates(fieldName, converted)
            }
            else {
                isValid = false
            }
            if (!isValid) {
                val sMin = String.format("%s", minDate)
                val sMax = String.format("%s", maxDate)
                context.addError(createError(Errors.FIELD_WRONG_DATE, fieldName, value,
                    if (minDate != null) sMin else "*", if (maxDate != null) sMax else "*"))
                return null
            }
        }

        // Time.
        if (minTime != null || maxTime != null) {
            if (converted is Long) {
                isValid = checkTimes(fieldName, converted)
            }
            else {
                isValid = false
            }
            if (!isValid) {
                addTimeRangeError(context, fieldName, value, minTime, maxTime)
                return null
            }
        }

        // Time.
        if (minInstant != null || maxInstant != null) {
            when (converted) {
                is OffsetDateTime -> {
                    isValid = checkTimes(fieldName, converted.toInstant())
                }
                is LocalDateTime -> {
                    isValid = checkTimes(fieldName, converted.atZone(timeZone).toInstant())
                }
                is Instant -> {
                    isValid = checkTimes(fieldName, converted)
                }
                else -> {
                    isValid = false
                }
            }
            if (!isValid) {
                addTimeRangeError(context, fieldName, value, minInstant, maxInstant)
                return null
            }
        }

        return converted
    }

    /**
     * Check (string) field length.
     * @param fieldName Name of the validated field.
     * @param value The field value to be validated.
     * @return True if the validation pass.
     */
    protected open fun checkLength(fieldName: String, value: String): Boolean {
        var valid = true
        val length = value.length
        if (minLength != null) {
            valid = length >= minLength!!
        }
        if (valid && maxLength != null) {
            valid = length <= maxLength!!
        }
        return valid
    }

    /**
     * Check (numeric) field range.
     * @param fieldName Name of the validated field.
     * @param value The field value to be validated.
     * @return True if the validation pass.
     */
    protected open fun checkRange(fieldName: String, value: Number?): Boolean {
        var valid = true
        if (value != null) {
            if (min != null) {
                valid = compare(min!!, value) != 1
            }
            if (valid && max != null) {
                valid = compare(max!!, value) != -1
            }
        }
        return valid
    }

    /**
     * Validate field value against given regular expression (regex).
     * @param fieldName Name of the validated field.
     * @param value The field value to be validated.
     * @return True if the validation pass.
     */
    protected open fun checkRegex(fieldName: String, value: String?): Boolean {
        var valid = true
        if (value != null && regex != null) {
            valid = regex!!.matcher(value).matches()
        }
        return valid
    }

    /**
     * Validate (date) field range.
     * @param fieldName Name of the validated field.
     * @param value The field value to be validated.
     * @return True if the validation pass.
     */
    protected open fun checkDates(fieldName: String, value: Date): Boolean {
        var valid = true
        if (minDate != null) {
            valid = value.time >= minDate!!.time
        }
        if (valid && maxDate != null) {
            valid = value.time <= maxDate!!.time
        }
        return valid
    }

    /**
     * Validate (time) field range.
     * @param fieldName Name of the validated field.
     * @param value The field value to be validated.
     * @return True if the validation pass.
     */
    protected open fun checkTimes(fieldName: String, value: Long): Boolean {
        var valid = true
        if (minTime != null) {
            valid = value >= minTime!!
        }
        if (valid && maxTime != null) {
            valid = value <= maxTime!!
        }
        return valid
    }

    /**
     * Validate (instant) field range.
     * @param fieldName Name of the validated field.
     * @param value The field value to be validated.
     * @return True if the validation pass.
     */
    protected open fun checkTimes(fieldName: String, value: Instant): Boolean {
        var valid = true
        if (minInstant != null) {
            valid = value >= minInstant!!
        }
        if (valid && maxInstant != null) {
            valid = value <= maxInstant!!
        }
        return valid
    }

    /**
     * Add a time range validation error to the context.
     * @param context Validation context to receive the error.
     * @param fieldName Name of the validated field.
     * @param value The field value that failed validation.
     * @param min Minimum allowed time boundary, or null if unbounded.
     * @param max Maximum allowed time boundary, or null if unbounded.
     */
    protected fun addTimeRangeError(context: ValidationContext, fieldName: String, value: Any?,
                                    min: Any?, max: Any?) {
        val sMin = String.format("%s", min)
        val sMax = String.format("%s", max)
        context.addError(createError(Errors.FIELD_WRONG_TIME, fieldName, value,
            if (min != null) sMin else "*", if (max != null) sMax else "*"))
    }

    /**
     * Compare two numbers (handles floating point and integer numbers).
     * @param n1 First number.
     * @param n2 Second number.
     * @return Comparison indicator:
     * * -1 if the first number is lower than the second number
     * * 1 if the first number is higher than the second number
     * * 0 if the two numbers are equal
     */
    protected open fun compare(n1: Number, n2: Number): Int {
        val ret: Int
        var a: Number = n1
        var b: Number = n2
        if (n1::class != n2::class) {
            // If the two numbers are from different classes, normalize both to double.
            a = n1.toDouble()
            b = n2.toDouble()
        }
        when (a) {
            is Byte -> {
                ret = a.compareTo(b as Byte)
            }
            is Short -> {
                ret = a.compareTo(b as Short)
            }
            is Float -> {
                ret = a.compareTo(b as Float)
            }
            is Int -> {
                ret = a.compareTo(b as Int)
            }
            is Double -> {
                ret = a.compareTo(b as Double)
            }
            is Long -> {
                ret = a.compareTo(b as Long)
            }
            is BigInteger -> {
                ret = a.compareTo(b as BigInteger)
            }
            is BigDecimal -> {
                ret = a.compareTo(b as BigDecimal)
            }
            else ->
                throw IllegalArgumentException(
                    "Unknown numeric class: ${a::class.java.canonicalName}")
        }
        return ret
    }

    /**
     * Convert provided value to specified field class.
     * @param value The value to be converted.
     * @param toClass Field class.
     * @return Converted value, or null if the value cannot be converted.
     */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    protected open fun toTargetClass(value: Any, toClass: Class<*>): Any? {
        var ret: Any? = null
        try {
            when (toClass) {
                Boolean::class.java -> {
                    if (value is Boolean) {
                        ret = value
                    }
                    else {
                        ret = when (value.toString()) {
                            "true", "yes", "y" -> true
                            else -> false
                        }
                    }
                }
                Byte::class.java -> {
                    if (value is Byte) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toByte()
                    }
                }
                Char::class.java -> {
                    if (value is Char) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toCharArray()[0]
                    }
                }
                Short::class.java -> {
                    if (value is Short) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toShort()
                    }
                }
                Int::class.java -> {
                    if (value is Int) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toInt()
                    }
                }
                Float::class.java -> {
                    if (value is Float) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toFloat()
                    }
                }
                Long::class.java -> {
                    if (value is Long) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toLong()
                    }
                }
                Double::class.java -> {
                    if (value is Double) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toDouble()
                    }
                }
                String::class.java -> {
                    ret = value.toString()
                }
                Date::class.java -> {
                    if (value is Date) {
                        ret = value
                    }
                    else {
                        val date = dFormatter.parse(value.toString(), LocalDate::from)
                        ret = Date.from(date.atStartOfDay().atZone(timeZone).toInstant())
                    }
                }
                LocalDate::class.java -> {
                    if (value is LocalDate) {
                        ret = value
                    }
                    else {
                        val date = dFormatter.parse(value.toString(), LocalDate::from)
                        ret = date.atStartOfDay()
                    }
                }
                LocalDateTime::class.java -> {
                    if (value is LocalDateTime) {
                        ret = value
                    }
                    else {
                        ret = tFormatter.parseBest(value.toString(), LocalDateTime::from)
                    }
                }
                OffsetDateTime::class.java -> {
                    if (value is OffsetDateTime) {
                        ret = value
                    }
                    else {
                        val parsed = tFormatter.parseBest(value.toString(),
                            OffsetDateTime::from, ZonedDateTime::from)
                        ret = if (parsed is ZonedDateTime) parsed.toOffsetDateTime()
                            else parsed as? OffsetDateTime
                    }
                }
                BigInteger::class.java -> {
                    if (value is BigInteger) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toBigInteger()
                    }
                }
                BigDecimal::class.java -> {
                    if (value is BigDecimal) {
                        ret = value
                    }
                    else {
                        ret = value.toString().toBigDecimal()
                    }
                }
                Serializable::class.java -> {
                    ret = value.toString()
                }
                else ->
                    ret = toCustomClass(value, toClass)
            }
        } catch (_: Exception) {
            // Conversion failed, but do not throw exception.
        }
        return ret
    }

    /**
     * Convert provided value to custom field class. This is extension point.
     * @param value - The value to be converted.
     * @param toClass - Field class.
     * @return Converted value, or null if the value cannot be converted.
     */
    protected open fun toCustomClass(value: Any, toClass: Class<*>): Any? {
        throw IllegalArgumentException("Unsupported conversion for class: ${toClass.name}")
    }
}
