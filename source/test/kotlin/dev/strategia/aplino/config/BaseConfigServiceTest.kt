package dev.strategia.aplino.config

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.security.BaseDataEncryptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

internal class BaseConfigServiceTest : TestBase() {
    companion object {
        const val COLOR = "Color"
        const val KEY_COLOR = "Theme_$COLOR"
        const val COLOR_YELLOW = "Yellow"
        const val COLOR_GREEN = "Green"
        const val COLOR_RED = "Red"
        const val FONT_SIZE = "FontSize"
        const val KEY_FONT_SIZE = "Theme_$FONT_SIZE"
        const val KEY_MISSING = "Missing"
        const val FONT_12 = "12"
    }

    @Test
    fun getValue() {
        val setting1 = ConfigSetting(COLOR_YELLOW)
        setting1.defaultValue = COLOR_GREEN
        setting1.description = "Theme color"
        val setting2 = ConfigSetting(null)
        setting2.defaultValue = FONT_12
        val settings = mapOf(KEY_COLOR to setting1, KEY_FONT_SIZE to setting2)
        val config = createConfigService(settings)
        var res: Any?
        res = config.getValue(KEY_COLOR)
        assertEquals(COLOR_YELLOW, res)
        res = config.getValue(KEY_COLOR)
        assertEquals(COLOR_YELLOW, res)
        res = config.getValue(KEY_MISSING)
        assertEquals(null, res)
        res = config.getValue(KEY_MISSING, COLOR_RED)
        assertEquals(COLOR_RED, res)
        res = config.getValue(KEY_FONT_SIZE)
        assertEquals(null, res)
        val setting = config.getSetting(KEY_COLOR)
        assertSame(setting1, setting)
    }

    @Test
    fun valueMandatory() {
        val settings = mapOf(KEY_COLOR to ConfigSetting(COLOR_YELLOW))
        val config = createConfigService(settings)
        val res = config.getValueMandatory(KEY_COLOR)
        assertEquals(COLOR_YELLOW, res)
        try {
            config.getValueMandatory(KEY_MISSING)
            fail()
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains(KEY_MISSING))
        }
    }

    @Test
    fun getValues() {
        val settings = mapOf(KEY_COLOR to ConfigSetting(COLOR_YELLOW),
            KEY_FONT_SIZE to ConfigSetting(FONT_12))
        val config = createConfigService(settings)
        val res = config.getValues(KEY_COLOR + "|" + KEY_FONT_SIZE)
        assertEquals(COLOR_YELLOW, res[KEY_COLOR])
        assertEquals(FONT_12, res[KEY_FONT_SIZE])
        assertEquals(2, res.size)
    }

    @Test
    fun getSettings() {
        val settingColor = ConfigSetting(COLOR_YELLOW)
        settingColor.defaultValue = "Green"
        settingColor.description = "Theme color"
        val settings = mapOf(KEY_COLOR to settingColor)
        val config = createConfigService(settings)
        val setting1 = config.getValue(KEY_COLOR)
        assertSame(COLOR_YELLOW, setting1)
        val settings2 = config.getSettingKeys("NoSuchKey")
        assertEquals(0, settings2.size)
        val settings3 = config.getSettingKeys("Theme.+")
        assertSame(KEY_COLOR, settings3.iterator().next())
    }

    @Test
    fun encryptValue() {
        val value = COLOR_YELLOW
        val config = createConfigService(mapOf())
        val encrypted = config.transformValue(value, Encryption.DO)
        assertNotEquals(value, encrypted)
        assertNotNull(encrypted)
        val decrypted = config.transformValue(encrypted.toString(), Encryption.UNDO)
        assertEquals(value, decrypted)
        val done = config.transformValue(value, Encryption.DONE)
        assertEquals(value, done)
        val none = config.transformValue(value, Encryption.NONE)
        assertEquals(value, none)
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun cyclicVariableReferenceDoesNotHang() {
        // A self-referencing variable must not cause an infinite replacement loop.
        val service = ExposedConfigService()
        val result = service.replaceVariables("\${a}", mapOf("a" to "\${a}"))
        assertNotNull(result)
    }

    private fun createConfigService(settings: Map<String, ConfigSetting?>): BaseConfigService {
        val configFile = workDir.absolutePath + "/testing/config/config1.conf"
        val service = BaseConfigService(workDir.path, configFile, null, BaseDataEncryptor())
        service.setSettings(settings)
        service.setEncryptionKey("key1")
        service.start()
        return service
    }

    private class ExposedConfigService : BaseConfigService("", null, null, null) {
        public override fun replaceVariables(value: String, variables: Map<String, Any?>): String? =
            super.replaceVariables(value, variables)
    }
}
