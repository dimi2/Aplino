package dev.strategia.aplino.application

/**
 * Common application constants. The client applications extend it.
 */
open class AppConstant protected constructor() {
    companion object {
        const val CONFIG_FILES = "CONFIG_FILES"
        const val LOG_CONFIG_FILE = "LOG_CONFIG_FILE"
        const val HOME_DIR = "HOME_DIR"
        const val TEMP_DIR = "TEMP_DIR"

        const val MODE = "MODE"
        const val PRODUCTION = "production"
        const val DEVELOPMENT = "development"
    }

}
