package dev.strategia.aplino.util

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Utilities specific for the operating system (OS).
 */
open class OsUtil {
    companion object {
        const val OS_NAME = "OsName"
        const val OS_VERSION = "OsVersion"
        const val OS_FULL = "OsFull"
        const val MEMORY_TOTAL = "MemoryTotal" // in megabytes.
        const val MEMORY_FREE = "MemoryFree" // in megabytes.
        const val MEMORY_AVAILABLE = "MemoryAvailable" // in megabytes.

        const val BYTES_PER_MEGABYTE = 1_000_000
        const val MEMORY_FIELDS_COUNT = 3

        /**
         * Gather operating system information (name, version, type).
         * @return Gathered information.
         */
        fun getOsInfo(): Map<String, String> {
            var osInfo = mutableMapOf<String, String>()
            val os = getOsType()
            if (os == OsType.Linux) {
                // Linux reports its kernel version instead of distribution version. Find better answer.
                val linReleaseInfo = runOsCommand("cat /etc/os-release", 1).inputReader()
                    .readText()
                if (linReleaseInfo.isNotBlank()) {
                    parseLinuxReleaseInfo(linReleaseInfo, osInfo)
                }

                // Retrieve memory information.
                val linMemoryInfo = runOsCommand("cat /proc/meminfo", 1).inputReader()
                    .readText()
                if (linMemoryInfo.isNotBlank()) {
                    parseLinuxMemoryInfo(linMemoryInfo, osInfo)
                }

            }
            else if (os == OsType.Windows) {
                // Windows reports confusing os name and version. Find better answer.
                val winReleaseInfo = runOsCommand("wmic os get caption,version /value")
                    .inputReader().readText().trim()
                if (winReleaseInfo.isNotBlank()) {
                    parseWindowsReleaseInfo(winReleaseInfo, osInfo)
                }

                // Retrieve memory information.
                val linMemoryInfo = runOsCommand(
                    "wmic OS get TotalVisibleMemorySize,FreePhysicalMemory,FreeVirtualMemory /Value", 1)
                    .inputReader().readText()
                if (linMemoryInfo.isNotBlank()) {
                    parseWindowsMemoryInfo(linMemoryInfo, osInfo)
                }
            }
            else if (os == OsType.Unknown) {
                // Cannot detect OS, fallback to JVM OS info.
                osInfo = sortedMapOf()
                osInfo[OS_NAME] = System.getProperty("os.name")
                osInfo[OS_VERSION] = System.getProperty("os.version")
                osInfo[OS_FULL] = osInfo[OS_NAME] + " " + osInfo[OS_VERSION]

                // Retrieve memory information.
                val rt = Runtime.getRuntime()
                osInfo[MEMORY_TOTAL] = (rt.totalMemory() / BYTES_PER_MEGABYTE).toString()
                osInfo[MEMORY_FREE] = (rt.freeMemory() / BYTES_PER_MEGABYTE).toString()
                val ma = rt.maxMemory() - rt.totalMemory() - rt.freeMemory()
                osInfo[MEMORY_AVAILABLE] = (ma / BYTES_PER_MEGABYTE).toString()
            }
            return osInfo
        }

        /**
         * Get the operating system type (Linux, Windows, Mac...).
         * @return The type.
         */
        fun getOsType(): OsType {
            var os = OsType.Unknown
            val osName = System.getProperty("os.name")?.lowercase()
            if (osName != null) {
                if (osName.contains("windows")) {
                    os = OsType.Windows
                }
                else if (osName.contains("linux") || osName.contains("mpe/ix")
                    || osName.contains("freebsd") || osName.contains("irix")) {
                    os = OsType.Linux
                }
                else if (osName.contains("digital unix") || osName.contains("unix")) {
                    os = OsType.Linux
                }
                else if (osName.contains("mac") || osName.contains("darwin")) {
                    os = OsType.Mac
                }
                else if (osName.contains("sun") || osName.contains("solaris")) {
                    os = OsType.Linux
                } else if (osName.contains("hp-ux") || osName.contains("aix")) {
                    os = OsType.Linux
                }
            }
            return os
        }

        /**
         * Get the version of the operating system.
         * @return The version.
         */
        fun getOsVersion(): String {
            var version: String? = null
            val os = getOsType()
            if (os == OsType.Linux) {
                // Linux reports its kernel version instead of distribution version. Find better answer.
                val osInfo = getOsInfo()
                version = osInfo[OS_VERSION]
            }
            // Cannot detect OS info. Fallback to JVM OS version.
            if (version == null) {
                version = System.getProperty("os.version") ?: "1"
            }
            return version
        }

        /**
         * Get current operating system directory.
         * @return Current directory.
         */
        fun getWorkDir(): File {
            return File(System.getProperty("user.dir") ?: "").absoluteFile
        }

        /**
         * Run native command from the operating system.
         * @param command The command to run.
         * @param timeoutSec Execution timeout (in seconds). If null, there is no timeout.
         * @return Command process. Its output could be taken with 'result.inputReader',
         * its exit code could be taken with 'result.exitValue()'.
         */
        fun runOsCommand(command: String, timeoutSec: Long? = null): Process {
            val splitCmd = command.split("\\s".toRegex())
            val workDir = getWorkDir()
            val pBuilder = ProcessBuilder(splitCmd).directory(workDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
            val process: Process
            if (timeoutSec != null) {
                process = pBuilder.start().also { it.waitFor(timeoutSec, TimeUnit.SECONDS) }
            }
            else {
                process = pBuilder.start()
            }
            return process
        }

        /**
         * Get current user of the operating system.
         * @return User name.
         */
        fun getCurrentUser(): String {
            return System.getProperty("user.name")
        }

        /**
         * Get the operating system user groups.
         * @param forUser Filter the groups to which given user belongs. If null, will not return all groups.
         * @return List of groups.
         */
        fun getOsGroups(forUser: String? = null): List<String> {
            val groups = mutableListOf<String>()
            val osType = getOsType()
            if (osType == OsType.Linux) {
                if (forUser == null) {
                    // All existing groups.
                    val outGroups = runOsCommand("getent group", 1).inputReader().readText()
                    if (outGroups.isNotBlank()) {
                        groups += parseLinuxGroupsAll(outGroups)
                    }
                }
                else {
                    // The groups for the given user.
                    if (!forUser.matches("^([a-z_][a-z0-9_-]*$?){1,32}$".toRegex())) {
                        throw IllegalArgumentException("Invalid user name: $forUser")
                    }
                    val outGroups = runOsCommand("groups $forUser", 1).inputReader().readText()
                    if (outGroups.isNotBlank()) {
                        groups += parseLinuxGroups(outGroups)
                    }
                }
            }
            else if (osType == OsType.Windows) {
                if (forUser == null) {
                    // All existing groups.
                    val outGroups = runOsCommand("net localgroup", 1).inputReader().readText()
                    if (outGroups.isNotBlank()) {
                        groups += parseWindowsGroupsAll(outGroups)
                    }
                }
                else {
                    // The groups for the given user.
                    if (!forUser.matches("^[a-zA-Z][a-zA-Z0-9\\-.]{0,61}[a-zA-Z][\\w\\- .]*\$".toRegex())) {
                        throw IllegalArgumentException("Invalid user name: $forUser")
                    }
                    val outGroups = runOsCommand("net user $forUser", 1).inputReader().readText()
                    if (outGroups.isNotBlank()) {
                        groups += parseWindowsGroups(outGroups)
                    }
                }
            }
            return groups
        }

        /**
         * Get free disk space, available to some file system location (directory). In containerized
         * environments, the application file system is read-only, while the temp/data files are stored in
         * configurable directory, mounted from some storage machine.
         *
         * @param forLocation The storage directory.
         * @return Free space in bytes.
         */
        fun getFreeSpace(forLocation: String): Long {
            return File(forLocation).freeSpace
        }

        protected fun parseLinuxReleaseInfo(txt: String, osInfo: MutableMap<String, String>) {
            val info = mutableMapOf<String, String>()
            txt.lines().forEach { line ->
                val key: String
                var value: String
                val sep = line.indexOf('=')
                if (sep != -1) {
                    key = line.substring(0, sep)
                    value = line.substring(sep + 1)
                    if (value.startsWith('"') && value.endsWith('"')) {
                        value = value.substring(1, value.length - 1)
                    }
                }
                else {
                    key = line
                    value = line
                }
                if (key.isNotEmpty()) {
                    info[key.uppercase()] = value
                }
            }

            // Normalize the info key names (not all distributions follow the key naming convention).
            val osName = info["NAME"]
            if (osName != null) {
                osInfo[OS_NAME] = osName
            }
            var osVersion = info["VERSION_ID"]
            if (osVersion != null) {
                osInfo[OS_VERSION] = osVersion
            }
            else {
                osVersion = info["VERSION"]
                if (osVersion != null) {
                    osInfo[OS_VERSION] = osVersion
                }
            }
            val osFull = info["PRETTY_NAME"]
            if (osFull != null) {
                osInfo[OS_FULL] = osFull
            }
            else {
                osInfo[OS_FULL] = "$osName $osVersion"
            }
        }

        protected fun parseWindowsReleaseInfo(txt: String, osInfo: MutableMap<String, String>) {
            val info = mutableMapOf<String, String>()
            txt.lines().forEach { line ->
                val sep = line.indexOf('=')
                if (sep != -1) {
                    val key = line.substring(0, sep).trim()
                    val value = line.substring(sep + 1).trim()
                    if (key.isNotEmpty()) {
                        info[key.uppercase()] = value
                    }
                }
            }

            // Normalize the info key names (the raw keys were stored upper-cased above).
            val osName = info["CAPTION"]
            if (osName != null) {
                osInfo[OS_NAME] = osName
            }
            val osVersion = info["VERSION"]
            if (osVersion != null) {
                osInfo[OS_VERSION] = osVersion
            }
            if (osName != null || osVersion != null) {
                osInfo[OS_FULL] = "$osName $osVersion"
            }
        }

        protected fun parseLinuxGroupsAll(txt: String): List<String> {
            val groups = mutableListOf<String>()
            txt.lines().forEach { line ->
                val sep = line.indexOf(':')
                if (sep != -1) {
                    groups.add(line.substring(0, sep))
                }
            }
            return groups.sorted()
        }

        protected fun parseLinuxGroups(txt: String): List<String> {
            val groups = mutableListOf<String>()
            val sep = txt.indexOf(':')
            if (sep != -1) {
                groups.addAll(txt.substring(sep + 1).trim().split(' '))
            }
            return groups
        }

        protected fun parseWindowsGroupsAll(txt: String): List<String> {
            val groups = mutableListOf<String>()
            txt.lines().forEach { line ->
                if (line.startsWith('*')) {
                    groups.add(line.substring(1).trim())
                }
            }
            return groups
        }

        protected fun parseWindowsGroups(txt: String): List<String> {
            val groups = mutableListOf<String>()
            val key1 = "Local Group Memberships"
            val key2 = "Global Group memberships"
            txt.lines().forEach { line ->
                if (line.startsWith(key1)) {
                    val gList = line.substring(key1.length).trim().split("*")
                    gList.forEach { g ->
                        if (g.isNotBlank()) {
                            groups.add(g.trim())
                        }
                    }
                }
                if (line.startsWith(key2)) {
                    val gList = line.substring(key2.length).trim().split("*")
                    gList.forEach { g ->
                        if (g.isNotBlank()) {
                            groups.add(g.trim())
                        }
                    }
                }
            }
            return groups
        }

        protected fun parseLinuxMemoryInfo(txt: String, osInfo: MutableMap<String, String>) {
            var added = 0
            for (line in txt.lines()) {
                if (added >= MEMORY_FIELDS_COUNT) {
                    break
                }
                val spLine = line.split(" ")
                if (spLine.size > 1) {
                    var key = spLine[0]
                    key = key.substring(0, key.length - 1)
                    val value = spLine[spLine.size - 2]
                    when (key) {
                        "MemTotal" -> {
                            osInfo[MEMORY_TOTAL] = value
                            added++
                        }
                        "MemFree" -> {
                            osInfo[MEMORY_FREE] = value
                            added++
                        }
                        "MemAvailable" -> {
                            osInfo[MEMORY_AVAILABLE] = value
                            added++
                        }
                    }
                }
            } //
        }

        protected fun parseWindowsMemoryInfo(txt: String, osInfo: MutableMap<String, String>) {
            var added = 0
            for (line in txt.lines()) {
                if (added >= MEMORY_FIELDS_COUNT) {
                    break
                }
                val spLine = line.split("=")
                if (spLine.size > 1) {
                    val key = spLine[0]
                    val value = spLine[1]
                    if (key == "TotalVisibleMemorySize") {
                        osInfo[MEMORY_TOTAL] = value
                        added++
                    }
                    else if (key == "FreePhysicalMemory") {
                        osInfo[MEMORY_FREE] = value
                        added++
                    }
                    else if (key == "FreeVirtualMemory") {
                        osInfo[MEMORY_AVAILABLE] = value
                        added++
                    }
                }
            } //
        }

    }

}
