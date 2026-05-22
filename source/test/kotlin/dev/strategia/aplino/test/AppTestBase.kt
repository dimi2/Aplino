package dev.strategia.aplino.test

/**
 * Base functionality for unit tests.
 */
abstract class AppTestBase {
    companion object {
        /**
         * Flag to indicate if the application context has been initialized (avoiding double initialization).
         */
        var initialized: Boolean = false
    }
    protected var workDir = TestUtil.setupWorkDirectory(null)
    protected var tempDir = TestUtil.setupTempDirectory(null, workDir)

}
