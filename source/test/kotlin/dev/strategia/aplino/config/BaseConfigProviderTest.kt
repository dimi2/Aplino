package dev.strategia.aplino.config

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class BaseConfigProviderTest : TestBase() {

    @Test
    fun readConfigFormat1() {
        val configFile = File(workDir, "testing/config/config1.conf")
        val provider = PropertiesConfigProvider()
        val configHolder = provider.load(configFile.absolutePath)
        assertEquals("Test configuration 1.", configHolder.comment)
        assertEquals(1, configHolder.format)
        val entries = configHolder.settings.entries.toList()

        val key1 = entries[0].key
        val setting1 = entries[0].value
        assertEquals("APP_DEFAULT_LOCALE", key1)
        assertEquals("de", setting1?.value)

        val key2 = entries[1].key
        val setting2 = entries[1].value
        assertEquals("APP_LOCALES", key2)
        assertEquals("en,bg,de", setting2?.value)
    }

    @Test
    fun writeConfigFormat1() {
        val file1 = File(workDir, "testing/config/config1.conf").absolutePath
        val file2 = File(workDir, "testing/config/config2.conf").absolutePath
        try {
            val provider1 = PropertiesConfigProvider()
            val configHolder1 = provider1.load(file1)
            provider1.store(file2, configHolder1)
            val provider2 = PropertiesConfigProvider()
            val configHolder2 = provider2.load(file2)
            assertEquals(configHolder1.format, configHolder2.format)
            assertEquals(configHolder1.comment, configHolder2.comment)
            val settings1 = configHolder1.settings.entries.toList()
            assertEquals("APP_DEFAULT_LOCALE", settings1[0].key)
            val settings2 = configHolder2.settings.entries.toList()
            assertEquals("APP_LOCALES", settings1[1].key)
            val setting1 = settings1[0].value as ConfigSetting
            val setting2 = settings2[0].value as ConfigSetting
            assertEquals(setting1.defaultValue, setting2.defaultValue)
            assertEquals(setting1.description, setting2.description)
            assertEquals(setting1.encryption, setting2.encryption)
            assertEquals(setting1.originalValue, setting2.originalValue)
            val setting11 = settings1[1].value as ConfigSetting
            val setting22 = settings2[1].value as ConfigSetting
            assertEquals(setting11.defaultValue, setting22.defaultValue)
            assertEquals(setting11.description, setting22.description)
            assertEquals(setting11.encryption, setting22.encryption)
            assertEquals(setting11.originalValue, setting22.originalValue)
        }
        finally {
            File(file2).delete()
        }
    }

}
