package dev.strategia.aplino.util

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.test.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.util.Locale

internal class DataUtilTest : TestBase() {

    @Test
    fun rounding() {
        val r1 = DataUtil.round(17.4960, 2)
        assertEquals(17.50, r1)
        val r2 = DataUtil.round(17.4960, 3)
        assertEquals(17.496, r2)
    }

    @Test
    fun stringMasking() {
        val mask = "\u0003".toCharArray()
        val initialData = String("DES/CFB8\u00eb".toByteArray(charset("Cp1250"))).toCharArray()
        val data = initialData.copyOf()
        DataUtil.mask(data, mask)
        assertNotEquals(data.concatToString(), initialData.concatToString())
        DataUtil.mask(data, mask)
        assertEquals(initialData.concatToString(), data.concatToString())
    }

    @Test
    fun maskToRegex() {
        val r1 = DataUtil.toRegexString("Bu*")
        assertEquals("^Bu.*?", r1)
        val r2 = DataUtil.toRegexString("*ia*")
        assertEquals(".*?ia.*?", r2)
        val r3 = DataUtil.toRegexString("*ia")
        assertEquals(".*?ia$", r3)
    }

    @Test
    fun localeCode() {
        val r1 = DataUtil.getLocaleCode(Locale.ENGLISH)
        assertEquals("en", r1)
        val r2 = DataUtil.getLocaleCode("en_US")
        assertEquals("en", r2)
    }

    @Test
    fun ensureStringSyntax() {
        DataUtil.ensureSyntax("s11", "\\w\\d+")
        DataUtil.ensureSyntax(null, "\\w?")
    }

    @Test
    fun encodeSql() {
        val res = DataUtil.escapeSql("x\\y = 'z'").toString()
        assertEquals("x\\\\y = ''z''", res)
    }

    @Test
    fun emptyStringConstant() {
        assertEquals("", DataUtil.EMPTY_STRING)
        assertTrue(DataUtil.EMPTY_STRING.isEmpty())
    }

    @Test
    fun steamClosing() {
        val testFile = TestUtil.writeFile("$tempDir/testClosing.txt", "testClosing")

        val stream = testFile.inputStream()
        DataUtil.close(stream)
        assertThrows<IOException> {
            assertEquals(0, stream.read())
        }
        testFile.delete()
    }
}
