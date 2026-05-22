package dev.strategia.aplino.validation

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.error.Errors
import dev.strategia.aplino.test.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Date

internal class ValidatorTest : TestBase() {
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_AGE = "age"
        const val FIELD_BIRTHDAY = "birthDay"
        const val FIELD_NUMBER = "number"
        const val FIELD_PURCHASE_TIME = "purchaseTime"
    }

    @Test
    fun validation1() {
        val validator = Validator()
        val constraints = BaseConstraint(String::class.java).mandatory(true).minLength(1).
            maxLength(5).regex("\\w+")
        validator.constrain(FIELD_NAME, constraints)
        var res: Any?
        val context1 = ValidationContext()
        res = validator.validateField(FIELD_NAME, null, context1)
        assertEquals(null, res)
        val errors1 = context1.getErrors()
        val error1 = errors1[0]
        assertEquals(Errors.FIELD_IS_EMPTY, error1.errorCode)
        assertEquals(1, errors1.size)
        val context2 = ValidationContext()
        val nameValue = "Robert"
        res = validator.validateField(FIELD_NAME, nameValue, context2)
        assertEquals(null, res)
        val errors2 = context2.getErrors()
        val error2 = errors2[0]
        assertEquals(Errors.FIELD_WRONG_SIZE, error2.errorCode)
        assertEquals(true, context2.hasErrors())
    }

    @Test
    fun validation2() {
        val validator = Validator()
        val constraints = BaseConstraint(Int::class.java).min(22).max(120)
        validator.constrain(FIELD_AGE, constraints)
        var res: Any?
        val context1 = ValidationContext()
        res = validator.validateField(FIELD_AGE, null, context1)
        assertEquals(null, res)
        val errors1 = context1.getErrors()
        assertEquals(0, errors1.size)
        val context2 = ValidationContext()
        res = validator.validateField(FIELD_AGE, 21, context2)
        assertEquals(null, res)
        val errors2 = context2.getErrors()
        val error2 = errors2[0]
        assertEquals(Errors.FIELD_WRONG_RANGE, error2.errorCode)
        assertEquals(true, context2.hasErrors())
    }

    @Test
    fun validation3() {
        val validator = Validator()
        val constraints = BaseConstraint(Float::class.java).range(2.72f, 3.14f)
        validator.constrain(FIELD_NUMBER, constraints)
        var res: Any?
        val context1 = ValidationContext()
        res = validator.validateField(FIELD_NUMBER, null, context1)
        assertEquals(null, res)
        val errors1 = context1.getErrors()
        assertEquals(0, errors1.size)
        val context2 = ValidationContext()
        res = validator.validateField(FIELD_NUMBER, 1, context2)
        assertEquals(null, res)
        val errors2 = context2.getErrors()
        val error2 = errors2[0]
        assertEquals(Errors.FIELD_WRONG_RANGE, error2.errorCode)
        assertEquals(true, context2.hasErrors())
    }

    @Test
    fun testValidation4() {
        val validator = Validator()
        val minDate: Date = TestUtil.asDate("2010-06-21")
        val maxDate: Date = TestUtil.asDate("2012-06-21")
        val constraints = BaseConstraint(Date::class.java).min(minDate).max(maxDate)
        validator.constrain(FIELD_BIRTHDAY, constraints)
        var res: Any?
        val context1 = ValidationContext()
        res = validator.validateField(FIELD_BIRTHDAY, null, context1)
        assertEquals(null, res)
        val errors1 = context1.getErrors()
        assertEquals(0, errors1.size)
        val context2 = ValidationContext()
        val date: Date = TestUtil.asDate("2020-10-23")
        res = validator.validateField(FIELD_BIRTHDAY, date, context2)
        assertEquals(null, res)
        val errors2 = context2.getErrors()
        val error2 = errors2[0]
        assertEquals(Errors.FIELD_WRONG_DATE, error2.errorCode)
        assertEquals(true, context2.hasErrors())
    }

    @Test
    fun testValidation5() {
        val zone = ZoneId.systemDefault()
        val validator = Validator()
        val minTime = LocalDateTime.of(2010, 6, 21, 22, 0, 0, 0).atZone(zone).toOffsetDateTime()
        val maxTime = LocalDateTime.of(2012, 6, 21, 23, 0, 0, 0).atZone(zone).toOffsetDateTime()
        val constraints = BaseConstraint(OffsetDateTime::class.java)
            .min(minTime).max(maxTime)
        validator.constrain(FIELD_PURCHASE_TIME, constraints)
        val context1 = ValidationContext()
        val res = validator.validateField(FIELD_PURCHASE_TIME, "2010-06-21 22:30:00", context1)
        val expected = LocalDateTime.of(2010, 6, 21, 22, 30, 0, 0).atZone(zone).toOffsetDateTime()
        assertEquals(expected, res)
        assertEquals(0, context1.getErrors().size)
    }

    @Test
    fun objectValidation() {
        val modelProperty = "model"
        val builtProperty = "built"
        val manufacturedProperty = "manufactured"
        val model = "Lotus"
        val manufactured = 2020
        val params = mapOf(modelProperty to model, builtProperty to manufactured)
        val context = ValidationContext()
        val validator = Validator(Car::class.java)
        validator.constrain(modelProperty, BaseConstraint(String::class.java).minLength(1))
        validator.constrain(builtProperty, manufacturedProperty, BaseConstraint(Int::class.java).min(2019))
        val obj = validator.validateObject(params::get, context)
        assertEquals(Car::class.java, obj::class.java)
        val car = obj as Car
        assertEquals(model, car.model)
        assertEquals(manufactured, car.manufactured)
        assertEquals(0, context.getErrorsCount())
    }

    @Test
    fun mapValidation() {
        val modelProperty = "model"
        val model = "Lotus"
        val params = mapOf(modelProperty to model)
        val context = ValidationContext()
        val validator = Validator(LinkedHashMap::class.java)
        validator.constrain(modelProperty, BaseConstraint(String::class.java).minLength(1))
        val obj = validator.validateObject(params::get, context)
        assertEquals(LinkedHashMap::class.java, obj::class.java)
        val map = obj as MutableMap<*, *>
        assertEquals(model, map[modelProperty])
        assertEquals(1, map.size)
    }

    class Car {
        var model: String? = null
        var manufactured: Int? = null
    }
}
