# Module Aplino

Application development framework in Kotlin for building structured, service-oriented applications.

## Overview

Aplino provides a central application context ([App][dev.strategia.aplino.application.App]) wired by
[AppBootstrap][dev.strategia.aplino.application.AppBootstrap], which initialises all core services in a
fixed order and exposes them through static accessors. Applications extend `AppBootstrap`, override
`init()` for custom setup, and swap individual service implementations by overriding the corresponding
`init*()` methods.

## Services

| Package | Purpose |
|---|---|
| [application][dev.strategia.aplino.application] | Central context, lifecycle, and constants |
| [config][dev.strategia.aplino.config] | Extended `.ini` file configuration with encryption support |
| [error][dev.strategia.aplino.error] | Centralised error handling with error codes and ticket IDs |
| [event][dev.strategia.aplino.event] | Event bus with abort/retry support |
| [log][dev.strategia.aplino.log] | Log4j2-backed logging, runtime-adjustable |
| [scheduler][dev.strategia.aplino.scheduler] | Cron-style job scheduler |
| [security][dev.strategia.aplino.security] | Encryption (BouncyCastle) and password hashing |
| [text][dev.strategia.aplino.text] | Localisation with runtime-updatable text entries |
| [validation][dev.strategia.aplino.validation] | Programmatic field validation without annotations |
| [util][dev.strategia.aplino.util] | Command-line parsing, file and OS utilities |
