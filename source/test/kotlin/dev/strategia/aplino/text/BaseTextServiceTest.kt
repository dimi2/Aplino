package dev.strategia.aplino.text

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.test.TestUtil.Companion.writeFile
import dev.strategia.aplino.util.FileUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Locale

private class ExposedTextService(defaultLocale: String, provider: TextProvider)
    : BaseTextService(defaultLocale, provider) {
    public override fun getParentLocale(fLocale: String): String = super.getParentLocale(fLocale)
}

internal class BaseTextServiceTest : TestBase() {

    private lateinit var messagesDir: File

    @BeforeEach
    fun begin() {
        messagesDir = File("$tempDir/messages")
        messagesDir.mkdirs()
    }

    @AfterEach
    fun end() {
        FileUtil.clearDirectory(messagesDir)
    }

    @Test
    fun localeLookup() {
        val fName1 = messagesDir.absolutePath + "/messages_en.res"
        writeFile(fName1, "key = valueEn")
        val fName2 = messagesDir.absolutePath + "/messages_de.res"
        writeFile(fName2, "key = valueDe")
        val fName3 = messagesDir.absolutePath + "/messages_de_DE.res"
        writeFile(fName3, "key = valueDeDe")
        val service = createTextService()

        val res = service.getTextEntry("key", "de")
        assertEquals("valueDe", res.text)
    }

    @Test
    fun localeFallbackToParent() {
        writeFile(messagesDir.absolutePath + "/messages_de.res", "key = valueDe")
        val service = createTextService()
        // 'de_DE' has no resources, so it falls back to the parent locale 'de'.
        val res = service.getTextEntry("key", "de_DE")
        assertEquals("valueDe", res.text)
    }

    @Test
    fun localeFallbackToDefault() {
        writeFile(messagesDir.absolutePath + "/messages_en.res", "key = valueEn")
        val service = createTextService()
        // 'fr' has no resources, so it falls back to the default locale 'en'.
        val res = service.getTextEntry("key", "fr")
        assertEquals("valueEn", res.text)
    }

    @Test
    fun missingKey() {
        val service = createTextService()
        val res = service.getText("missingKey", Locale.ENGLISH.language)
        assertEquals("missingKey", res)
    }

    @Test
    fun formattedText() {
        val fName1 = messagesDir.absolutePath + "/messages_en.res"
        writeFile(fName1, "key = {0} O'Henry")
        val service = createTextService()
        val res = service.getText("key", Locale.ENGLISH.language, 3)
        assertEquals("3 O'Henry", res)
    }

    @Test
    fun getParentLocaleWithSuffix() {
        val service = ExposedTextService(Locale.ENGLISH.language,
            PropertiesTextProvider(messagesDir.absolutePath))
        assertEquals("de", service.getParentLocale("de_DE"))
        assertEquals("zh", service.getParentLocale("zh-sg"))
    }

    @Test
    fun getParentLocaleSimple() {
        val service = ExposedTextService(Locale.ENGLISH.language,
            PropertiesTextProvider(messagesDir.absolutePath))
        assertEquals("haw", service.getParentLocale("haw"))
    }

    private fun createTextService(): BaseTextService {
        val service = BaseTextService(Locale.ENGLISH.language,
            PropertiesTextProvider(messagesDir.absolutePath))
        service.start()
        return service
    }
}
