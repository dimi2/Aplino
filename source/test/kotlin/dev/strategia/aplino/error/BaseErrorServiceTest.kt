package dev.strategia.aplino.error

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.File

internal class BaseErrorServiceTest : TestBase() {
    companion object {
        const val CANNOT_READ_FILE = "R1000"
    }

    @Test
    fun getErrorInfo() {
        val errorDetails = "Cannot read file '%s'. Check the name and the file permissions"
        val errorParam = "file1.txt"
        val provider = MapErrorInfoProvider()
        provider.errors[CANNOT_READ_FILE]  = ErrorInfo(CANNOT_READ_FILE, errorDetails)
        val errorService = createService(provider)
        try {
            errorService.throwError(this, CANNOT_READ_FILE, errorDetails, null, null, errorParam)
            fail()
        } catch (expected: Exception) {
            val exception = expected as ApplicationException
            assertEquals(errorDetails, exception.errorInfo?.details)
        }
    }

    @Test
    fun errorDetailsAreSubstituted() {
        val errorDetails = "Cannot read file '%s'. Check the name and the file permissions"
        val provider = MapErrorInfoProvider()
        provider.errors[CANNOT_READ_FILE] = ErrorInfo(CANNOT_READ_FILE, errorDetails)
        val errorService = createService(provider)
        try {
            errorService.throwError(this, CANNOT_READ_FILE, errorDetails, null, null, "file1.txt")
            fail()
        } catch (expected: Exception) {
            val exception = expected as ApplicationException
            // The exception message has the parameters substituted in.
            assertEquals("Cannot read file 'file1.txt'. Check the name and the file permissions",
                exception.message)
            // The stored details template is left unchanged.
            assertEquals(errorDetails, exception.errorInfo?.details)
        }
    }

    @Test
    fun applicationException() {
        val ex = ApplicationException("runtime error")
        assertEquals("runtime error", ex.message)
        assertNull(ex.cause)
    }

    @Test
    fun applicationExceptionWithCause() {
        val cause = RuntimeException("root cause")
        val ex = ApplicationException("wrapped", cause)
        assertEquals("wrapped", ex.message)
        assertEquals(cause, ex.cause)
    }

    @Test
    fun securityException() {
        val ex1 = SecurityException("access denied")
        assertEquals("access denied", ex1.message)
        assertNull(ex1.errorInfo)
        val cause = RuntimeException("root cause")
        val ex2 = SecurityException("access denied", cause)
        assertEquals("access denied", ex2.message)
        assertEquals(cause, ex2.cause)
        val info = ErrorInfo("S001", "forbidden")
        val ex3 = SecurityException(info, null)
        assertEquals(info, ex3.errorInfo)
    }

    @Test
    fun dataTransferException() {
        val ex = DataTransferException("connection reset")
        assertEquals("connection reset", ex.message)
        assertNull(ex.cause)
    }

    @Test
    fun configurationException() {
        val ex1 = ConfigurationException("bad setting")
        assertEquals("bad setting", ex1.message)
        val cause = RuntimeException("root cause")
        val ex2 = ConfigurationException("bad setting", cause)
        assertEquals("bad setting", ex2.message)
        assertEquals(cause, ex2.cause)
        val info = ErrorInfo("C001", "invalid port")
        val ex3 = ConfigurationException(info, null)
        assertEquals(info, ex3.errorInfo)
    }

    @Test
    fun dataStructureException() {
        val ex = DataStructureException("protocol violation")
        assertEquals("protocol violation", ex.message)
    }

    @Test
    fun errorInfoId() {
        val info = ErrorInfo("E001", "some error")
        assertNull(info.id)
        info.id = 42
        assertEquals(42, info.id)
    }

    @Test
    fun deadlocksDump() {
        val dumpFile = "$tempDir/deadlocks.txt"
        JvmDumper.createDeadlocksDump(dumpFile)
        assertTrue(File(dumpFile).exists())
    }

    private fun createService(provider: ErrorInfoProvider): BaseErrorService {
        val service = BaseErrorService(provider)
        service.start()
        return service
    }
}
