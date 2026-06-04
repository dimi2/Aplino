package dev.strategia.aplino.validation

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CheckedConstraintTest : TestBase() {

    val yellow = "Yellow"
    val green = "Green"

    @Test
    fun typicalUse() {
        val context = ValidationContext()
        val constraint = CheckedConstraint(true, null, ::customChecker1)

        val value1 = constraint.accept("color", yellow, context)
        assertEquals(0, context.getErrorsCount())
        assertEquals(yellow, value1)

        val value2 = constraint.accept("color", "Brown", context)
        assertEquals(1, context.getErrorsCount())
        assertEquals(null, value2)
    }

    @Test
    fun withConverter() {
        val context = ValidationContext()
        val constraint = CheckedConstraint(true, ::customConverter, ::customChecker2)

        val value = constraint.accept("coordinates", "80 20", context) as PositivePoint
        assertEquals(0, context.getErrorsCount())
        assertEquals(80, value.x)
        assertEquals(20, value.y)
    }

    private fun customConverter(value: Any): Any {
        val parts = value.toString().split(' ')
        val vX = parts[0].toInt()
        val vY = parts[1].toInt()
        return PositivePoint(vX, vY)
    }

    private fun customChecker1(value: Any?): Boolean {
        return listOf(yellow, green).contains(value)
    }

    private fun customChecker2(value: Any?): Boolean {
        val obj = value as PositivePoint
        return (obj.x > 0 && obj.y > 0)
    }

    private class PositivePoint(val x: Int, val y: Int)
}
