# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew dist        # Full build: checkEnv → build → publish to local Ivy repo
./gradlew build       # Compile and test only
./gradlew test        # Run all tests
./gradlew clean       # Remove build/, work/program/, work/proof/, work/data/log/, work/data/temp/
./gradlew createJar      # Package the framework into a JAR
```

To run a single test class:
```bash
./gradlew test --tests "dev.strategia.aplino.config.BaseConfigServiceTest"
```

Minimum required versions (enforced by `checkEnv`): Java 25, Gradle 9.5.1, Kotlin 2.3.21.

## Source Layout

This project uses non-standard Gradle source directories to avoid resource file duplication:

- `source/main/kotlin/` — main sources, compiled to `work/program/`
- `source/test/kotlin/` — test sources, compiled to `work/proof/`
- `build/libs/` — output JARs

There is no `resources/` directory. Application resource files (config, logging config) live under `work/` or `data/` at runtime, accessed relative to `App.home()`.

## Architecture

### Application Context (`App` / `AppBootstrap`)

`App` is the central context object accessed entirely through its companion object (static methods). It holds references to all core services and a general-purpose `registry` map for application-wide objects.

`AppBootstrap` creates and wires the `App` instance. Applications subclass `AppBootstrap`, override `init()` for custom service setup, and override individual `init*()` methods to swap service implementations. Initialization order is fixed: mode → home directory → encryption → logging → config → errors → localization → events → `init()`.

```kotlin
class MyBootstrap : AppBootstrap() {
    override fun init(application: App, parameters: Map<String, String?>) {
        application.set("myService", MyService())
    }
}
```

### Service Pattern

All services implement `AppService` (with `start()` / `stop()` lifecycle hooks). Each service has:
- An interface (e.g., `LogService`, `ConfigService`, `ErrorService`)
- A `Base*` open class providing the default implementation
- Extension via subclassing the `Base*` class

Services are accessed via `App.*()` static methods (`App.log()`, `App.config()`, `App.error()`, `App.event()`, `App.encryption()`).

### Core Packages

| Package | Key Classes | Purpose |
|---|---|---|
| `application` | `App`, `AppBootstrap`, `AppConstant` | Context, lifecycle, constants |
| `config` | `BaseConfigService`, `ConfigSetting`, `PropertiesConfigProvider` | Extended `.ini` file config with encryption support |
| `error` | `BaseErrorService`, `ErrorInfo`, `ApplicationException` | Centralized error handling with error codes and ticket IDs |
| `event` | `BaseEventService`, `BaseAppEvent`, `EventListener` | Event bus with abort/retry support |
| `log` | `BaseLogService`, `LogService` | Log4j2-backed logging, runtime-adjustable |
| `scheduler` | `BaseSchedulerService`, `SchedulerJob` | Cron-style job scheduler (backed by Sundial) |
| `security` | `BaseDataEncryptor`, `AdvancedDataEncryptor`, `Argon2PasswordEncoder`, `Pbkdf2PasswordEncoder` | Encryption (BouncyCastle) and password hashing |
| `text` | `BaseTextService`, `PropertiesTextProvider` | Localization with runtime-updatable text entries |
| `validation` | `Validator`, `Constraint`, `ValidationContext` | Programmatic field validation without annotations |
| `util` | `CommandLineParser`, `FileUtil`, `OsUtil`, `PropertiesFileParser`, `ValueHolder` | Utilities |

### Validation

Constraints are added programmatically to a `Validator` instance, then `validateObject()` or `validateField()` is called. `ValidationContext` (or `FileValidationContext`) collects errors. No annotation-based validation is used.

### Publishing

`dist` publishes the `createJar` artifact to the local Ivy repository at `~/.gradle/local` using group `dev.strategia`, module `aplino`.

## Testing

Tests extend `TestBase` (in the default package) which extends `AppTestBase`. `AppTestBase` sets up `workDir` and `tempDir` via `TestUtil`. Tests requiring the application context use `AppTestBase.initialized` flag to avoid double initialization.
