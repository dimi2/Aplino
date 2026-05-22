package dev.strategia.aplino.application

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.error.ApplicationException
import dev.strategia.aplino.error.BaseErrorService
import dev.strategia.aplino.event.BaseEventService
import dev.strategia.aplino.log.BaseLogService
import dev.strategia.aplino.security.BaseDataEncryptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AppTest : TestBase() {

    @BeforeAll
    fun setupApp() {
        if (!initialized) {
            val app = App()
            val logService = BaseLogService(null).also { it.start() }
            val errorService = BaseErrorService().also { it.start() }
            val eventService = BaseEventService().also { it.start() }
            app.setLogService(logService)
            app.setErrorService(errorService)
            app.setEventService(eventService)
            app.setEncryptor(BaseDataEncryptor())
            app.setMode(AppConstant.DEVELOPMENT)
            initialized = true
        }
    }

    @Test
    fun logging() {
        assertNotNull(App.logging())
    }

    @Test
    fun event() {
        assertNotNull(App.event())
    }

    @Test
    fun mode() {
        assertEquals(AppConstant.DEVELOPMENT, App.mode())
    }

    @Test
    fun getDefaultLocale() {
        val locale = App.getDefaultLocale()
        assertNotNull(locale)
        assert(locale.isNotBlank())
    }

    @Test
    fun setDefaultLocale() {
        App().setDefaultLocale("fr")
        assertEquals("fr", App.getDefaultLocale())
        App().setDefaultLocale("en")
    }

    @Test
    fun throwError() {
        assertThrows<ApplicationException> {
            App.throwError(this, "E001", "test error details")
        }
    }
}
