// Project plugins registry.
pluginManagement {
    val minKotlinVersion: String by settings
    val dokkaVersion: String by settings
    val detektVersion: String by settings
    val nmcpVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version minKotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("dev.detekt") version detektVersion
        id("com.gradleup.nmcp.settings") version nmcpVersion
        id("ivy-publish")
        id("maven-publish")
        id("idea")
    }
}

plugins {
    id("com.gradleup.nmcp.settings")
}

nmcpSettings {
    centralPortal {}
}
