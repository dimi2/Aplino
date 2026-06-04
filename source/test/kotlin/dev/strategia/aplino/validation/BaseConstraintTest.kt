package dev.strategia.aplino.validation

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.error.Errors
import dev.strategia.aplino.test.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date

internal class BaseConstraintTest : TestBase() {

    @Test
    fun mandatoryWithValue() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).mandatory(true)
        val result = constraint.accept("field", "value", context)
        assertEquals("value", result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun mandatoryWithNull() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).mandatory(true)
        val result = constraint.accept("field", null, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_IS_EMPTY, context.getErrors()[0].errorCode)
    }

    @Test
    fun mandatoryWithBlankString() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).mandatory(true)
        val result = constraint.accept("field", "   ", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_IS_EMPTY, context.getErrors()[0].errorCode)
    }

    @Test
    fun notMandatoryWithNull() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).mandatory(false)
        val result = constraint.accept("field", null, context)
        assertNull(result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun lengthWithinRange() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).length(2, 10)
        val result = constraint.accept("field", "hello", context)
        assertEquals("hello", result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun lengthTooShort() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).length(5, 10)
        val result = constraint.accept("field", "hi", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_SIZE, context.getErrors()[0].errorCode)
    }

    @Test
    fun lengthTooLong() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).length(2, 5)
        val result = constraint.accept("field", "hello world", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_SIZE, context.getErrors()[0].errorCode)
    }

    @Test
    fun minLengthOnly() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).minLength(3)
        val result = constraint.accept("field", "ab", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_SIZE, context.getErrors()[0].errorCode)
    }

    @Test
    fun minClassMismatch() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Double::class.java).min(0)
        val result = constraint.accept("field", 1.0, context)
        assertEquals(0, context.getErrorsCount())
        assertEquals(1.0, result)
    }

    @Test
    fun maxLengthOnly() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).maxLength(5)
        val result = constraint.accept("field", "hello world", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_SIZE, context.getErrors()[0].errorCode)
    }

    @Test
    fun rangeWithinRange() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java).range(10, 100)
        val result = constraint.accept("field", 50, context)
        assertEquals(50, result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun rangeBelowMin() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java).range(10, 100)
        val result = constraint.accept("field", 5, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_RANGE, context.getErrors()[0].errorCode)
    }

    @Test
    fun rangeAboveMax() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java).range(10, 100)
        val result = constraint.accept("field", 150, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_RANGE, context.getErrors()[0].errorCode)
    }

    @Test
    fun minOnly() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java).min(10)
        val result = constraint.accept("field", 5, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_RANGE, context.getErrors()[0].errorCode)
    }

    @Test
    fun maxOnly() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java).max(100)
        val result = constraint.accept("field", 150, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_RANGE, context.getErrors()[0].errorCode)
    }

    @Test
    fun regexMatch() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).regex("[a-z]+")
        val result = constraint.accept("field", "hello", context)
        assertEquals("hello", result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun regexNoMatch() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java).regex("[a-z]+")
        val result = constraint.accept("field", "hello123", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_SYNTAX, context.getErrors()[0].errorCode)
    }

    @Test
    fun dateWithinRange() {
        val context = ValidationContext()
        val minDate = TestUtil.asDate("2010-01-01")
        val maxDate = TestUtil.asDate("2020-12-31")
        val constraint = BaseConstraint(Date::class.java).min(minDate).max(maxDate)
        val testDate = TestUtil.asDate("2015-06-15")
        val result = constraint.accept("field", testDate, context)
        assertNotNull(result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun dateBeforeMin() {
        val context = ValidationContext()
        val minDate = TestUtil.asDate("2010-01-01")
        val maxDate = TestUtil.asDate("2020-12-31")
        val constraint = BaseConstraint(Date::class.java).min(minDate).max(maxDate)
        val testDate = TestUtil.asDate("2005-06-15")
        val result = constraint.accept("field", testDate, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_DATE, context.getErrors()[0].errorCode)
    }

    @Test
    fun dateAfterMax() {
        val context = ValidationContext()
        val minDate = TestUtil.asDate("2010-01-01")
        val maxDate = TestUtil.asDate("2020-12-31")
        val constraint = BaseConstraint(Date::class.java).min(minDate).max(maxDate)
        val testDate = TestUtil.asDate("2025-06-15")
        val result = constraint.accept("field", testDate, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_DATE, context.getErrors()[0].errorCode)
    }

    @Test
    fun dateTimeWithinRange() {
        val context = ValidationContext()
        val minTime = TestUtil.asDateTime("2010-01-01T00:00:00")
        val maxTime = TestUtil.asDateTime("2020-12-31T23:59:59")
        val constraint = BaseConstraint(OffsetDateTime::class.java).min(minTime).max(maxTime)
        val testTime = TestUtil.asDateTime("2015-06-15T12:30:00")
        val result = constraint.accept("field", testTime, context)
        assertNotNull(result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun dateTimeBeforeMin() {
        val context = ValidationContext()
        val minTime = TestUtil.asDateTime("2010-01-01T00:00:00")
        val maxTime = TestUtil.asDateTime("2020-12-31T23:59:59")
        val constraint = BaseConstraint(OffsetDateTime::class.java).min(minTime).max(maxTime)
        val testTime = TestUtil.asDateTime("2005-06-15T12:30:00")
        val result = constraint.accept("field", testTime, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_TIME, context.getErrors()[0].errorCode)
    }

    @Test
    fun minTimeWithLong() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Long::class.java).minTime(1000L)
        val result = constraint.accept("field", 500L, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_TIME, context.getErrors()[0].errorCode)
    }

    @Test
    fun maxTimeWithLong() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Long::class.java).maxTime(1000L)
        val result = constraint.accept("field", 2000L, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_TIME, context.getErrors()[0].errorCode)
    }

    @Test
    fun minTimeWithInstant() {
        val context = ValidationContext()
        val minInstant = Instant.parse("2010-01-01T00:00:00Z")
        val constraint = BaseConstraint(OffsetDateTime::class.java).minTime(minInstant)
        val testTime = TestUtil.asDateTime("2005-06-15T12:30:00")
        val result = constraint.accept("field", testTime, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_TIME, context.getErrors()[0].errorCode)
    }

    @Test
    fun maxTimeWithInstant() {
        val context = ValidationContext()
        val maxInstant = Instant.parse("2010-01-01T00:00:00Z")
        val constraint = BaseConstraint(OffsetDateTime::class.java).maxTime(maxInstant)
        val testTime = TestUtil.asDateTime("2015-06-15T12:30:00")
        val result = constraint.accept("field", testTime, context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_TIME, context.getErrors()[0].errorCode)
    }

    @Test
    fun dateFormatCustomPattern() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Date::class.java).dateFormat("dd/MM/yyyy")
        val result = constraint.accept("field", "15/06/2015", context)
        assertNotNull(result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun timeFormatCustomPattern() {
        val context = ValidationContext()
        val constraint = BaseConstraint(OffsetDateTime::class.java).timeFormat("dd/MM/yyyy HH:mm:ss")
        val result = constraint.accept("field", "15/06/2015 12:30:00", context)
        assertNotNull(result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun chainedConstraints() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java)
            .mandatory(true)
            .length(3, 10)
            .regex("[a-z]+")
        val result = constraint.accept("field", "hello", context)
        assertEquals("hello", result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun convertToBoolean() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Boolean::class.java)
        assertEquals(true, constraint.accept("field", true, context))
        assertEquals(true, constraint.accept("field", "true", context))
        assertEquals(true, constraint.accept("field", "yes", context))
        assertEquals(true, constraint.accept("field", "y", context))
        assertEquals(false, constraint.accept("field", false, context))
        assertEquals(false, constraint.accept("field", "false", context))
    }

    @Test
    fun convertToByte() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Byte::class.java)
        assertEquals(42.toByte(), constraint.accept("field", 42.toByte(), context))
        assertEquals(42.toByte(), constraint.accept("field", "42", context))
    }

    @Test
    fun convertToChar() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Char::class.java)
        assertEquals('a', constraint.accept("field", 'a', context))
        assertEquals('a', constraint.accept("field", "a", context))
    }

    @Test
    fun convertToShort() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Short::class.java)
        assertEquals(1000.toShort(), constraint.accept("field", 1000.toShort(), context))
        assertEquals(1000.toShort(), constraint.accept("field", "1000", context))
    }

    @Test
    fun convertToInt() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java)
        assertEquals(42, constraint.accept("field", 42, context))
        assertEquals(42, constraint.accept("field", "42", context))
    }

    @Test
    fun convertToFloat() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Float::class.java)
        assertEquals(3.14f, constraint.accept("field", 3.14f, context))
        assertEquals(3.14f, constraint.accept("field", "3.14", context))
    }

    @Test
    fun convertToLong() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Long::class.java)
        assertEquals(1000000L, constraint.accept("field", 1000000L, context))
        assertEquals(1000000L, constraint.accept("field", "1000000", context))
    }

    @Test
    fun convertShortToLong() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Long::class.java)
        val result = constraint.accept("field", 1000.toShort(), context)
        assertEquals(1000L, result)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun convertToDouble() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Double::class.java)
        assertEquals(3.14159, constraint.accept("field", 3.14159, context))
        assertEquals(3.14159, constraint.accept("field", "3.14159", context))
    }

    @Test
    fun convertToString() {
        val context = ValidationContext()
        val constraint = BaseConstraint(String::class.java)
        assertEquals("hello", constraint.accept("field", "hello", context))
        assertEquals("123", constraint.accept("field", 123, context))
    }

    @Test
    fun convertToDate() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Date::class.java)
        val date = TestUtil.asDate("2015-06-15")
        assertNotNull(constraint.accept("field", date, context))
        assertNotNull(constraint.accept("field", "2015-06-15", context))
    }

    @Test
    fun convertToLocalDate() {
        val context = ValidationContext()
        val constraint = BaseConstraint(LocalDate::class.java)
        val localDate = LocalDate.of(2015, 6, 15)
        assertEquals(localDate, constraint.accept("field", localDate, context))
        assertNotNull(constraint.accept("field", "2015-06-15", context))
    }

    @Test
    fun convertToLocalDateTime() {
        val context = ValidationContext()
        val constraint = BaseConstraint(LocalDateTime::class.java)
        val localDateTime = LocalDateTime.of(2015, 6, 15, 12, 30)
        assertEquals(localDateTime, constraint.accept("field", localDateTime, context))
    }

    @Test
    fun convertToOffsetDateTime() {
        val context = ValidationContext()
        val constraint = BaseConstraint(OffsetDateTime::class.java)
        val offsetDateTime = OffsetDateTime.of(2015, 6, 15, 12, 30, 0, 0, ZoneOffset.UTC)
        assertEquals(offsetDateTime, constraint.accept("field", offsetDateTime, context))
        assertNotNull(constraint.accept("field", "2015-06-15T12:30:00Z", context))
    }

    @Test
    fun convertToBigInteger() {
        val context = ValidationContext()
        val constraint = BaseConstraint(BigInteger::class.java)
        val bigInt = BigInteger("12345678901234567890")
        assertEquals(bigInt, constraint.accept("field", bigInt, context))
        assertEquals(bigInt, constraint.accept("field", "12345678901234567890", context))
    }

    @Test
    fun convertToBigDecimal() {
        val context = ValidationContext()
        val constraint = BaseConstraint(BigDecimal::class.java)
        val bigDec = BigDecimal("1234567890.1234567890")
        assertEquals(bigDec, constraint.accept("field", bigDec, context))
        assertEquals(bigDec, constraint.accept("field", "1234567890.1234567890", context))
    }

    @Test
    fun toCustomClassUnsupported() {
        val constraint = BaseConstraint(BaseConstraintTest::class.java)
        val context = ValidationContext()
        val result = constraint.accept("field", "test", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
    }

    @Test
    fun invalidConversion() {
        val context = ValidationContext()
        val constraint = BaseConstraint(Int::class.java)
        val result = constraint.accept("field", "not_a_number", context)
        assertNull(result)
        assertEquals(1, context.getErrorsCount())
        assertEquals(Errors.FIELD_WRONG_SYNTAX, context.getErrors()[0].errorCode)
    }

    @Test
    fun removeError() {
        val context = ValidationContext()
        val error = ValidationError(1, "bad value", "field1")
        context.addError(error)
        assertTrue(context.hasErrors())
        val removed = context.removeError(error)
        assertTrue(removed)
        assertFalse(context.hasErrors())
    }

    @Test
    fun throwExceptionOnError() {
        val context = ValidationContext()
        context.throwExceptionOnError(true)
        val error = ValidationError(1, "bad value", "field1")
        assertThrows<ValidationException> {
            context.addError(error)
        }
    }

    @Test
    fun resetContext() {
        val context = ValidationContext()
        context.addError(ValidationError(1, "err", "f"))
        context.currentObject = "someObject"
        context.locale = "de"
        context.reset()
        assertFalse(context.hasErrors())
        assertNull(context.currentObject)
        assertNull(context.locale)
    }

    @Test
    fun validationExceptionWithCause() {
        val cause = RuntimeException("root cause")
        val exception = ValidationException("failed", cause)
        assertEquals("failed", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun validationExceptionAddError() {
        val exception = ValidationException("failed")
        val error = ValidationError(1, "bad value", "field1")
        val added = exception.addError(error)
        assertTrue(added)
        assertEquals(1, exception.errors.size)
    }

    @Test
    fun fileValidationContext() {
        val ctx = FileValidationContext()
        ctx.currentRow = 5
        ctx.currentColumn = "amount"
        ctx.currentFile = "data.csv"
        ctx.currentOffset = 1024L
        assertEquals(5, ctx.currentRow)
        assertEquals("amount", ctx.currentColumn)
        assertEquals("data.csv", ctx.currentFile)
        assertEquals(1024L, ctx.currentOffset)
    }
}
