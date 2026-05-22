package dev.strategia.aplino.application

import dev.strategia.aplino.application.AppConstant.Companion.HOME_DIR
import dev.strategia.aplino.config.BaseConfigService
import dev.strategia.aplino.config.ConfigService
import dev.strategia.aplino.error.BaseErrorService
import dev.strategia.aplino.error.ErrorService
import dev.strategia.aplino.event.BaseEventService
import dev.strategia.aplino.event.EventService
import dev.strategia.aplino.log.BaseLogService
import dev.strategia.aplino.log.LogService
import dev.strategia.aplino.security.BaseDataEncryptor
import dev.strategia.aplino.security.DataEncryptor
import java.io.File

/**
 * Application bootstrap initializer. It creates the [application context][App] and execute the
 * application initialization sequence. When it finishes the application starts working.
 * Custom applications extend this class and customize the initialization process and the services.
 *
 * The created application context ([App]) provides common functionality which every serious application needs
 * (configuration, logging, event dispatcher, service registry etc.). The application context also gives
 * access to the application services. The configuration paths are relative to [App.home] directory.
 *
 * Application resources reside to directory under application home, outside the sources. They will not be
 * copied to other (build) directory so there will be only one copy for them. This requires control of the
 * compilation directories inside the build script, because it is different from default behavior of these
 * tools. See the project build script for example.
 *
 * The implementation uses reasonable defaults for most used functionality. It also provides multiple
 * extension points, so te be easy to extend and override the defaults.
 */
open class AppBootstrap {
    // Default application directories and files. For containerized deployments, the "data" directory
    // will be mounted as writable volume. This way, configuration could be changed without rebuilding
    // application container image.
    protected var configName = "data/config/config.ini"
    protected var buildConfigName = "data/config/build.ini"
    protected var logConfigName = "data/config/logging.properties"
    protected var tempDirName = "data/temp"
    protected var configPrefix = ""

    /** Application start parameters (usually from command line). */
    protected lateinit var parameters: Map<String, String?>

    /**
     * Initialize the application (execute the initialization sequence) before starting it.
     * @see start
     */
    @Synchronized
    open fun initApp(parameters: Map<String, String?> = mapOf()) {
        // Create the application context (facade).
        // Each initialization step has own method which could be overriden if necessary.
        val application = App()

        val mode = initMode(parameters)
        if (mode != null) {
            application.setMode(mode)
        }

        val homeDir = detectHomeDirectory().absolutePath
        application.setHomeDirectory(homeDir)
        System.setProperty(configPrefix + HOME_DIR, homeDir)

        val encryptor = initEncryption(parameters)
        application.setEncryptor(encryptor)

        val logService = initLogging(parameters)
        application.setLogService(logService)

        val configService = initConfig(homeDir, logService, encryptor, parameters)
        application.setConfigService(configService)

        val errorService = initErrors(parameters)
        application.setErrorService(errorService)

        setupLocalization(application, parameters)

        val eventService = initEvents(parameters)
        application.setEventService(eventService)

        // Base services are ready. Now allow custom initializations.
        init(application, parameters)
    }

    /**
     * Start the application (after the initialization sequence is finished).
     *
     * @see initApp
     * @see stop
     */
    open fun start() {
    }

    /**
     * Stop the application, gracefully. Note that this method exeuction is not guaranteed - the JVM may halt
     * at any time because of internal failure or insufficient memory.
     *
     * @see start
     */
    @Synchronized
    open fun stop() {
    }

    /**
     * Custom initializations, called after base services are initialized. Here should be initialized the
     * application specific services.
     * @param application The application context (still initializable).
     * @param parameters Execution parameters.
     * @see initApp
     */
    protected open fun init(application: App, parameters: Map<String, String?>) {
    }

    /**
     * Detect the application home directory. It expects the application classes to be in directory under
     * the application home directory. For example "AppDir/program". Some build setups (default for Gradle
     * and Maven) would compile in "AppDir/build/classes/main/kotlin". An IDE could default to
     * "AppDir/out/main", or "AppDir/bin". Here the application itself knows its home directory to avoid
     * issues with relative paths to its resouce files.
     * @return The application home directory.
     */
    protected open fun detectHomeDirectory(): File {
        val userDir = File(System.getProperty("user.dir"))
        val classLocation = javaClass.protectionDomain.codeSource.location.toURI().path!!
        val classDir = File(classLocation).parentFile
        val homeDir = if (classDir.parentFile == userDir) classDir else userDir

        // Check the directory permissions.
        if (!homeDir.isDirectory) {
            throw IllegalArgumentException("Invalid application directory '$homeDir'. " +
                "Check the directory existence and its access permissions.")
        }

        return homeDir
    }

    /**
     * Set up the directory for storing temporary files (it should be writeable).
     * @param tempDirectory Requested temporary directory. If not specified, assume the default.
     * @return The temporary directory.
     */
    protected open fun setupTempDirectory(tempDirectory: String? = null): File {
        val path = tempDirectory ?: tempDirName
        val f = File(path)
        var tempDir = if (f.isAbsolute) f else File(detectHomeDirectory(), path)
        if (!tempDir.isDirectory) {
            // Try to create the temporary directory if it does not exist.
            tempDir.mkdirs()
        }
        if (!tempDir.isDirectory || !tempDir.canWrite()) {
            // Try with the JVM temp directory.
            val subDir = File(App.home()).name
            tempDir = File(System.getProperty("java.io.tmpdir"), subDir)
            tempDir.mkdirs()
        }
        return tempDir
    }

    /**
     * Initialize the application execution mode ("profile").
     * @param parameters Execution parameters.
     * @return Mode name.
     */
    protected open fun initMode(parameters: Map<String, String?>): String? {
        val mode = parameters[configPrefix + AppConstant.MODE]
        return mode
    }

    /**
     * Initialize the encryption service of the application.
     * @param parameters Execution parameters.
     * @return Data encryptor instance.
     */
    protected open fun initEncryption(parameters: Map<String, String?>): DataEncryptor {
        // Use the JDK provided encryption, without additional dependencies.
        return BaseDataEncryptor()
    }

    /**
     * Initialize the logging service of the application.
     * @param parameters Execution parameters.
     * @return Logging service instance.
     */
    protected open fun initLogging(parameters: Map<String, String?>): LogService {
        val paramConfigFile = configPrefix + AppConstant.LOG_CONFIG_FILE
        var configFileName = parameters[paramConfigFile] ?: System.getProperty(paramConfigFile)
        if (configFileName == null) {
            configFileName = logConfigName
        }

        var configFile: String? = null
        if (configFileName.isNotEmpty()) {
            // Is the config file specified with relative path?
            val cf = File(configFileName)
            if (!cf.isAbsolute) {
                configFile = App.home() + File.separator + configFileName
            }
            else {
                configFile = cf.absolutePath
            }
        }

        val service = BaseLogService(configFile)
        service.start()
        return service
    }

    /**
     * Initialize the configuration service for the application.
     * @param logService Log service to use.
     * @param parameters Execution parameters.
     * @return Configuration service instance.
     */
    protected open fun initConfig(homeDir: String, logService: LogService, encryptor: DataEncryptor,
                                  parameters: Map<String, String?>): ConfigService {
        // Is config file configured?
        val paramConfigFiles = configPrefix + AppConstant.CONFIG_FILES
        var configFileNames = parameters[paramConfigFiles] ?: System.getProperty(paramConfigFiles)
        if (configFileNames == null) {
            configFileNames = "$buildConfigName,$configName"
        }

        // Create config service which uses that config location.
        val service = BaseConfigService(homeDir, configFileNames, logService, encryptor, parameters)
        service.start()
        return service
    }

    /**
     * Initialize the error service for the application.
     * @param parameters Execution parameters.
     * @return Error service instance.
     */
    protected open fun initErrors(parameters: Map<String, String?>): ErrorService {
        val service = BaseErrorService()
        service.start()
        return service
    }

    /**
     * Initialize the the locale and time zone.
     * @param application The application instance.
     * @param parameters Execution parameters.
     */
    protected open fun setupLocalization(application: App, parameters: Map<String, String?>) {
    }

    /**
     * Initialize the event dispatch service for the application.
     * @param parameters Execution parameters.
     * @return Event service instance.
     */
    protected open fun initEvents(parameters: Map<String, String?>): EventService {
        val service = BaseEventService()
        service.start()
        return service
    }

}
