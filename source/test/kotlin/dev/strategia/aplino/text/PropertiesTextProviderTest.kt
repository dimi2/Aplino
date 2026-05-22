package dev.strategia.aplino.text

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.test.TestUtil.Companion.writeFile
import dev.strategia.aplino.util.FileUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Locale

internal class PropertiesTextProviderTest : TestBase() {

    private lateinit var messagesDir: File

    @BeforeEach
    @Throws(Exception::class)
    fun begin() {
        messagesDir = File("$tempDir/messages")
        messagesDir.mkdirs()
    }

    @AfterEach
    fun end() {
        FileUtil.clearDirectory(messagesDir)
    }

    @Test
    fun missingResource() {
        try {
            PropertiesTextProvider(messagesDir.absolutePath + "/missing")
            fail()
        } catch (_: IllegalArgumentException) {
            // Expected behavior.
        }
    }

    @Test
    fun specialChars() {
        val fName = messagesDir.absolutePath + "/messages.res"
        writeFile(fName, "key = valu\u00eb")
        val provider = PropertiesTextProvider(messagesDir.absolutePath)
        val res = provider.getTextEntry("key", "")
        assertEquals("valu\u00eb", res!!.text)
    }

    @Test
    fun multiLanguage() {
        val fName1 = messagesDir.absolutePath + "/messages_en.res"
        writeFile(fName1, "key = valueEn")
        val fName2 = messagesDir.absolutePath + "/messages_de.res"
        writeFile(fName2, "key = valueDe")
        val fName3 = messagesDir.absolutePath + "/messages_de_DE.res"
        writeFile(fName3, "key = valueDeDe")
        var res: TextEntry?
        val provider = PropertiesTextProvider(messagesDir.absolutePath)
        res = provider.getTextEntry("key", "de_DE")
        assertEquals("valueDeDe", res!!.text)
        res = provider.getTextEntry("key", Locale.GERMAN.language)
        assertEquals("valueDe", res!!.text)
        res = provider.getTextEntry("key", Locale.ENGLISH.language)
        assertEquals("valueEn", res!!.text)
    }

    @Test
    fun clearTextEntries() {
        val fName = messagesDir.absolutePath + "/messages_en.res"
        writeFile(fName, "key = original")
        val provider = PropertiesTextProvider(messagesDir.absolutePath)
        assertEquals("original", provider.getTextEntry("key", "en")!!.text)
        writeFile(fName, "key = updated")
        provider.clearTextEntries()
        assertEquals("updated", provider.getTextEntry("key", "en")!!.text)
    }

    @Test
    fun mapTextProvider() {
        val provider = MapTextProvider()
        provider.setMessages("en", mapOf("greeting" to "Hello", "farewell" to "Goodbye"))
        val entry = provider.getTextEntry("greeting", "en")
        assertNotNull(entry)
        assertEquals("greeting", entry!!.key)
        assertEquals("Hello", entry.text)
        assertNull(provider.getTextEntry("greeting", "de"))
    }

    @Test
    fun testTextEntries() {
        val fName = messagesDir.absolutePath + "/messages.res"
        writeFile(fName, "key1 = value1\nkey2=value2")
        val provider = PropertiesTextProvider(messagesDir.absolutePath)
        val res = provider.getTextEntries("key.+", "")
        val entry1 = res[0]
        assertEquals("key1", entry1.key)
        assertEquals("value1", entry1.text)
        val entry2 = res[1]
        assertEquals("key2", entry2.key)
        assertEquals("value2", entry2.text)
    }
}
