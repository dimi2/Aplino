package dev.strategia.aplino.log

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.application.AppConstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class BaseLogServiceTest : TestBase() {

    @Test
    fun defaultLogging() {
        val logFileName = "aplino.log"
        val service = getService()
        val errorMessage = "Test error message 1"

        service.getLogger(this).error(errorMessage, RuntimeException("Test exception 1"))
        val logFile = File(tempDir, logFileName)
        val logLines = logFile.readLines()
        val line1 = logLines[0].split(';')
        assertEquals("ERROR", line1[1])
        assertEquals(errorMessage, line1[3])
    }

    private fun getService(): BaseLogService {
        // Referred variable in the logging config file. Here boostrap is not executed and it was not set.
        System.setProperty(AppConstant.TEMP_DIR, tempDir.absolutePath)

        val service = BaseLogService(workDir.absolutePath + "/testing/config/logging1.properties")
        service.start()
        return service
    }
}
