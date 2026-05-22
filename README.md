# Aplino Application Framework

**Aplino** is a small application framework for Kotlin. It provides base functionality (logging, configuration, error handling, events, scheduling, localization, encryption, and validation) **without** a dependency injection container, component scanning, or annotation processing.

Applications start in milliseconds. There are no hidden proxies, no reflective bean wiring, and no startup phases to debug. You write a straightforward bootstrap class, call it, and your application is running.

## Why Aplino

It is alternative of the Spring Framework. The Spring Framework works, but it introduces costs:

- **Startup overhead** - bean scanning, proxy generation, and context refresh add seconds to every startup and every test run, even for simple applications.
- **Self-referential complexity** - many Spring features exist to address problems Spring itself introduced. Qualifiers fix ambiguous autowiring. Scopes patch shared-state issues. The framework grows to manage its own complexity, including even banner support feature.
- **Hidden dependency graph** - annotation-driven injection obscures how components are connected. Tracing why a bean was injected (or not) often requires a debugger.
- **Abundant dependency injection** - The inversion of control container adds "layer of fat" over the application, just for sake of dependency injection. It handles lifecycle of application components, most of which are set anyway only once in constructor and live as singletons. Testability and decoupling can definitely exist without dependency injection.   

Aplino replaces the container with explicitness. Dependencies are wired in a single `init()` method you write yourself. The application context is one plain object. There is nothing to scan, nothing to proxy, and nothing to configure beyond what your application actually needs.
This framework provides common application building functionality. Specific extensions like HTTP server or ORM are not included - their choice depend on the actual needs and architecture. 

## Quick Start

Subclass `AppBootstrap`, wire your services in `init()`, then launch:

```kotlin
class MyApp : AppBootstrap() {
    override fun init(application: App, parameters: Map<String, String?>) {
        application.set("orderService", OrderService())
    }
}

fun main(args: Array<String>) {
    MyApp().start(args)
}
```

Access framework services from anywhere in your application:

```kotlin
App.log().info("Order received")
App.config().get(DatabaseSetting.HOST)
App.event().publish(OrderCreatedEvent(order))
```

The functionality initialization order is: mode - home directory - encryption - logging - config - errors - localization - events - your custom `init()` where the specific services/components are created.

## Key Features

- Global application context, via the `App` class (created from `AppBootstrap`).
- Centralized logging service, via `BaseLogService`. No static logger per class. Logging can be adapted at runtime when needed to investigate a problem.
- Centralized error handling service, via `BaseErrorService`. Provides error recovery support and error code classification.
- Application event service, via `BaseEventService`. Supports event abort, retry, and component decoupling.
- Configuration service, via `BaseConfigService`. By default, uses extended properties files (to minimize dependencies), but can be extended to support YAML, XML, or other formats.
- Different execution modes (production, development).
- Flexible validation, via `Validator`. No annotations, no overuse of temporary objects.
- Localization, via `BaseTextService`. Supports runtime-updatable translations beyond static resource files, which is useful for product catalogs and post-release content updates.
- Data encryption, via `DataEncryptor`.
- Tiny action scheduler, via `BaseSchedulerService`.
- Utilities for: command line parsing (`CommandLineParser`), OS detection (`OsUtil`), extended properties files with multiline values (`PropertiesFileParser`), common file operations (`FileUtil`), mutable call parameters (`ValueHolder`).

## Technical Specifications

- Programming Language: Kotlin
- Build System: Gradle
- Testing Framework: JUnit

## Build and Distribution

Default task:
```bash
./gradlew dist
```
This task runs environment checks, builds the project, and publishes the artifacts to the local repository.

Other useful tasks:
- `clean`: Cleans build and custom output directories.
- `createJar`: Packages the framework into a JAR file.
- `generateDocs`: Generates project documentation via Dokka.
- `publishAllPublicationsToCentralPortal`: Generate and publish project artifacts to Maven Central (manual operation, outside the distribution build). 

## History

This work is rewrite of older framework (written in Java) from Dimitar Kapitanov. The original name was Ronve - the demon of wisdom. It was an answer to "overengineering" and "bloatware" trends.

6329bb69ad6f164919bc7282f0047d7402a4754d90a6ecfbdcd151301b037202

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)