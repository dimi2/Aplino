package dev.strategia.aplino.util

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class FileUtilTest : TestBase() {

    private val testDir = File(tempDir, "fileutil-tests")

    @AfterEach
    fun cleanup() {
        FileUtil.clearDirectory(testDir)
        testDir.delete()
    }

    @Test
    fun getExistingFile() {
        testDir.mkdirs()
        val f = File(testDir, "sample.txt").also { it.writeText("x") }
        val result = FileUtil.getFile(f.absolutePath)
        assertNotNull(result)
        assertTrue(result.isFile)
    }

    @Test
    fun getMissingFile() {
        assertThrows<IllegalArgumentException> {
            FileUtil.getFile("/nonexistent/path/does-not-exist.txt")
        }
    }

    @Test
    fun getExistingDirectory() {
        testDir.mkdirs()
        val result = FileUtil.getDirectory(testDir.absolutePath)
        assertNotNull(result)
        assertTrue(result.isDirectory)
    }

    @Test
    fun getMissingDirectory() {
        assertThrows<IllegalArgumentException> {
            FileUtil.getDirectory("/nonexistent/path/does-not-exist")
        }
    }

    @Test
    fun moveFile() {
        testDir.mkdirs()
        val source = File(testDir, "source.txt").also { it.writeText("content") }
        val target = File(testDir, "target.txt").absolutePath
        val moved = FileUtil.moveFile(source.absolutePath, target)
        assertTrue(moved)
        assertFalse(source.exists())
        assertTrue(File(target).exists())
    }

    @Test
    fun moveFileNullArgs() {
        val result = FileUtil.moveFile(null, null)
        assertFalse(result)
    }

    @Test
    fun deleteFile() {
        testDir.mkdirs()
        val f = File(testDir, "to-delete.txt").also { it.writeText("x") }
        val deleted = FileUtil.deleteFile(f.absolutePath)
        assertTrue(deleted)
        assertFalse(f.exists())
    }

    @Test
    fun deleteFileMissing() {
        val result = FileUtil.deleteFile("/nonexistent/path/ghost.txt")
        assertFalse(result)
    }

    @Test
    fun deleteDirectory() {
        val dir = File(testDir, "nested").also { it.mkdirs() }
        File(dir, "inner.txt").writeText("x")
        val deleted = FileUtil.deleteDirectory(dir.absolutePath)
        assertTrue(deleted)
        assertFalse(dir.exists())
    }

    @Test
    fun getExtension() {
        assertEquals(".pdf", FileUtil.getExtension("report.pdf"))
        assertEquals(".kt", FileUtil.getExtension("Main.kt"))
    }

    @Test
    fun getExtensionNoExt() {
        assertEquals("", FileUtil.getExtension("README"))
    }

    @Test
    fun getExtensionNull() {
        assertEquals("", FileUtil.getExtension(null))
    }
}
