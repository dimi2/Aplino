package dev.strategia.aplino.validation

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CollectionConstraintTest : TestBase() {

    @Test
    fun listOfStrings() {
        val context = ValidationContext()
        val constraint = CollectionConstraint(ArrayList::class.java,
            BaseConstraint(String::class.java).maxLength(1))

        val value1 = listOf("A", "B", "C")
        val converted1 = constraint.accept("list", value1, context)
        assertEquals(false, context.hasErrors())
        assertEquals(value1, converted1)

        val value2 = "A, B, C"
        val converted2 = constraint.accept("list", value2, context)
        assertEquals(false, context.hasErrors())
        assertEquals(value1, converted2)
    }
}
