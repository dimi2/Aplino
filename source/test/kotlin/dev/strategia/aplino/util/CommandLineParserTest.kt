package dev.strategia.aplino.util

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CommandLineParserTest : TestBase() {

    @Test
    fun normalUse() {
        val args = arrayOf("-a", "1", "-b", "2", "c")
        val cmd = CommandLineParser(args)
        assertEquals(2, cmd.getArguments().size)
        assertEquals(true, cmd.hasArgument("a"))
        assertEquals("1", cmd.getArgumentValue("a"))
        assertEquals("2", cmd.getArgumentValue("b"))
        assertEquals(null, cmd.getArgumentValue("c"))
    }

    @Test
    fun oneValue() {
        val args = arrayOf("-help")
        val cmd = CommandLineParser(args)
        assertEquals(true, cmd.hasArgument("help"))
    }

    @Test
    fun parseArguments() {
        val args = arrayOf("-a", "1", "-b", "2", "c")
        val cmd = CommandLineParser()
        val p: Map<String, String?> = cmd.parseArguments(args)
        assertEquals(2, p.size)
        assertEquals("1", p["a"])
        assertEquals("2", p["b"])
        val values = cmd.getValues()
        assertEquals(3, values.size)
        assertEquals("1", values[0])
    }

    @Test
    fun doubleQuotedValue() {
        val args = arrayOf("-name", "\"John Doe\"")
        val cmd = CommandLineParser(args)
        assertEquals("John Doe", cmd.getArgumentValue("name"))
    }

    @Test
    fun singleQuotedValue() {
        val args = arrayOf("-name", "'Jane Roe'")
        val cmd = CommandLineParser(args)
        assertEquals("Jane Roe", cmd.getArgumentValue("name"))
    }

    @Test
    fun multipleValues() {
        val args = arrayOf("-a", "1", "b", "c")
        val cmd = CommandLineParser(args)
        val values = cmd.getValues()
        assertEquals(3, values.size)
        assertEquals("1", values[0])
        assertEquals("b", values[1])
        assertEquals("c", values[2])
    }
}
