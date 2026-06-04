package dev.strategia.aplino.util

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.test.TestUtil.Companion.writeFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class PropertiesFileParserTest : TestBase() {

    @Test
    fun readKey() {
        val testFile = "$tempDir/test1.res"
        val s = "key1 = value1"
        writeFile(testFile, s)
        val res = PropertiesFileParser().readFile(testFile)
        assertEquals("value1", res["key1"])
    }

    @Test
    fun emptyValue() {
        val testFile = "$tempDir/test1.res"
        val s = "key1 = "
        writeFile(testFile, s)
        val res = PropertiesFileParser().readFile(testFile)
        assertEquals(null, res["key1"])
    }

    @Test
    fun multiLineValue() {
        val testFile = "$tempDir/test2.res"
        val s = """
            key2 = << END
            line1
            line2
            END
            """.trimIndent()
        writeFile(testFile, s)
        val r = PropertiesFileParser()
        val res = r.readFile(testFile)
        assertEquals("line1\nline2\n", res["key2"])
    }

    @Test
    fun multiLineValueKeepsCommentAndBlankLines() {
        val testFile = "$tempDir/test4.res"
        // Inside a multi-line value, '#' lines and blank lines are content, not comments/separators.
        val s = "key = << END\nline1\n# not a comment\n\nline2\nEND"
        writeFile(testFile, s)
        val res = PropertiesFileParser().readFile(testFile)
        assertEquals("line1\n# not a comment\n\nline2\n", res["key"])
    }

    @Test
    fun writeComment() {
        val testFile = "$tempDir/commented.properties"
        PropertiesFileWriter(testFile).use { writer ->
            writer.writeComment("hello world")
            writer.writeProperty("key1", "value1")
        }
        val lines = File(testFile).readLines()
        assertTrue(lines[0].startsWith("#"))
        assertTrue(lines[0].contains("hello world"))
    }

    @Test
    fun multiLineValues() {
        val testFile = "$tempDir/test3.res"
        val s = """
            key2 =<< AAA
            line1
            AAA
            key3= <<EEE
            line2
            EEE
            """.trimIndent()
        writeFile(testFile, s)
        val r = PropertiesFileParser()
        val res = r.readFile(testFile)
        assertEquals("line1\n", res["key2"])
        assertEquals("line2\n", res["key3"])
    }

}
