package dev.strategia.aplino.test

import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Application test utility. Provides functionality for :
 * - Setup of working directory. To be same as application execution directory after deployment.
 * - Temporary storage directory for files, produced by tests.
 * - Read/write complete files as strings.
 * - Date to string and back conversions.
 */
internal class TestUtil {
    companion object {
        private var dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE!!
        private var timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm:ss][.SSS][Z]")
            .withZone(ZoneOffset.UTC)!!

        fun setupWorkDirectory(dir: String?): File {
            var workDir: File
            if (dir == null) {
                // Detect the application home directory.
                val userDir = File(System.getProperty("user.dir"))
                val classLocation = javaClass.protectionDomain.codeSource.location.toURI().path!!
                val classDir = File(classLocation).parentFile
                workDir = if (classDir.parentFile == userDir) classDir else userDir
            }
            else {
                // Try the provided directory.
                workDir = File(dir).canonicalFile
            }

            // Check the directory permissions.
            if (!workDir.isDirectory) {
                throw IllegalArgumentException("Invalid work directory '$workDir'. " +
                    "Check the permissions.")
            }

            // Change the work directory.
            System.setProperty("user.dir", workDir.absolutePath)
            return workDir
        }


        /**
         * Setup temporary storage directory for tests.
         * @param dir Temporary directory name (relative to project root directory).
         * Null - to use default.
         */
        fun setupTempDirectory(dir: String?, workDir: File? = null): File {
            var tempDir: File
            if (dir == null) {
                tempDir = File(workDir, "data/temp").absoluteFile
            }
            else {
                tempDir = File(dir).absoluteFile
            }
            tempDir.mkdirs()
            if (!tempDir.canWrite()) {
                val systemTempDir = System.getProperty("java.io.tmpdir")
                tempDir = File(systemTempDir).absoluteFile
            }
            return tempDir
        }

        /**
         * Write specified content to a file. If the file does not exist, it will be created, if it already
         * exists - it will be deleted first.
         * The method is intended for test purposes - use it for small files.
         * @param name File name.
         * @param content Content to be written into the file.
         * @return Created file.
         */
        fun writeFile(name: String, content: String): File {
            val file = File(name).absoluteFile
            file.parentFile.mkdirs()
            file.delete()
            file.appendText(content)
            return file
        }

        /**
         * Convert ISO date string to date.
         * @param isoDate Date string in ISO format.
         * @return Date object.
         */
        fun asDate(isoDate: String): Date {
            val date: LocalDate = dateFormatter.parse(isoDate, LocalDate::from)
            return Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC))
        }

        /**
         * Convert ISO date string to date.
         * @param isoDate Date string in ISO format.
         * @return Date object.
         */
        fun asDateTime(isoDate: String): OffsetDateTime {
            return timeFormatter.parse(isoDate, OffsetDateTime::from)
        }

    }
}
