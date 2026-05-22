package dev.strategia.aplino.util

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class OsUtilTest: TestBase() {

    @Test
    fun detectOsType() {
        val osType = OsUtil.getOsType()
        assertNotNull(osType)
    }

    @Test
    fun gatherOsInfo() {
        val osInfo = OsUtil.getOsInfo()
        assertNotNull(osInfo[OsUtil.OS_NAME])
        assertNotNull(osInfo[OsUtil.OS_VERSION])
        assertNotNull(osInfo[OsUtil.OS_FULL])
        assertNotNull(osInfo[OsUtil.MEMORY_TOTAL])
        assertNotNull(osInfo[OsUtil.MEMORY_FREE])
        assertNotNull(osInfo[OsUtil.MEMORY_AVAILABLE])
    }

    @Test
    fun getOsVersion() {
        val osVersion = OsUtil.getOsVersion()
        assertTrue(osVersion.length > 1)
    }

    @Test
    fun osGroups() {
        val groups = OsUtil.getOsGroups()
        assertTrue(groups.size >= 2)
    }

    @Test
    fun userGroups() {
        val groups = OsUtil.getOsGroups(OsUtil.getCurrentUser())
        assertTrue(groups.isNotEmpty())
    }

    @Test
    fun getFreeSpace() {
        val freeSpace = OsUtil.getFreeSpace(tempDir.absolutePath)
        assertTrue(freeSpace >= 0)
    }

}
