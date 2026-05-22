package dev.strategia.aplino.util

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValueHolderTest : TestBase() {

    @Test
    fun testValue() {
        val n = 7
        val holder = ValueHolder(n)
        assertEquals(n, holder.value)
    }
}
