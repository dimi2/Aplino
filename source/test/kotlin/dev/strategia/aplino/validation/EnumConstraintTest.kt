package dev.strategia.aplino.validation

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EnumConstraintTest : TestBase() {

    @Test
    fun fromString() {
        val context = ValidationContext()
        val constraint = EnumConstraint(EnumColor::class.java)

        val value1 = constraint.accept("color", "RED", context)
        assertEquals(EnumColor.RED, value1)

        val value2 = constraint.accept("color", "Red", context)
        assertEquals(null, value2)
    }

    @Test
    fun ignoreCase() {
        val context = ValidationContext()
        val constraint = EnumConstraint(EnumColor::class.java, isMandatory = false, ignoreCase = true)

        val value1 = constraint.accept("color", "Green", context)
        assertEquals(EnumColor.GREEN, value1)

        val value2 = constraint.accept("color", "green", context)
        assertEquals(EnumColor.GREEN, value2)
    }

    enum class EnumColor {
        RED, GREEN
    }
}
