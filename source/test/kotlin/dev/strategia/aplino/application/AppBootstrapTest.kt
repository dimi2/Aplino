package dev.strategia.aplino.application

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class AppBootstrapTest : TestBase() {

    val testDir = File(tempDir, "bootstrap-tests")

    private val bootstrap = object : AppBootstrap() {
        public override fun setupTempDirectory(tempDirectory: String?) =
            super.setupTempDirectory(tempDirectory)
    }

    @AfterEach
    fun cleanup() {
        testDir.deleteRecursively()
    }

    @Test
    fun setupTempDirectoryWithNull() {
        val result = bootstrap.setupTempDirectory(null)
        assertTrue(result.isDirectory)
        assertTrue(result.canWrite())
        assertTrue(result.path.endsWith("data" + File.separator + "temp"))
    }

    @Test
    fun setupTempDirectoryWithExistingDir() {
        testDir.mkdirs()
        val result = bootstrap.setupTempDirectory(testDir.absolutePath)
        assertEquals(testDir.absolutePath, result.absolutePath)
        assertTrue(result.isDirectory)
    }

    @Test
    fun setupTempDirectoryCreatesNewDir() {
        val newDir = File(testDir, "created")
        assertFalse(newDir.exists())
        val result = bootstrap.setupTempDirectory(newDir.absolutePath)
        assertTrue(result.isDirectory)
        assertEquals(newDir.absolutePath, result.absolutePath)
    }

    @Test
    fun setupTempDirectoryFallsBackToJvmTempDir() {
        App().setHomeDirectory(workDir.absolutePath)
        testDir.mkdirs()
        val blocker = File(testDir, "blocked.txt").also { it.writeText("x") }
        val result = bootstrap.setupTempDirectory(blocker.absolutePath)
        val expected = File(System.getProperty("java.io.tmpdir"), workDir.name)
        assertEquals(expected.absolutePath, result.absolutePath)
        assertTrue(result.isDirectory)
    }
}
