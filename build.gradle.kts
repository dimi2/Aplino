// Gradle build script for the project.

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("dev.detekt")
    id("com.gradleup.nmcp")
    id("ivy-publish")
    id("maven-publish")
    idea
    signing
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

// Project dependencies.
dependencies {
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.26.0"))
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl")
    implementation("org.bouncycastle:bcprov-jdk18on:1.84")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:${project.extra["minKotlinVersion"]}"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.knowm:sundial:2.4.0")
    implementation(kotlin("reflect"))

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Define project specific directories (we do not use the default project structure
// because it is inefficient - requires resource files copying).
layout.buildDirectory = file("build")
val workDir = "work"
val autoDocDir = "${layout.buildDirectory.get()}/autodoc"
val programDir = "$workDir/program"
val testsDir = "$workDir/proof"
val tempDir = "$workDir/data/temp"
val logDir = "$workDir/data/log"

// Use custom source directories structure. This is to control compilation output directories. It also
// prevents unnecessary file copy. There is no 'resources' directory to be copied by default on every build.
// That would end up with two copies of each resource, also the relative path to them is different
// in development/testing/production mode.
sourceSets {
    main {
        kotlin {
            kotlin.setSrcDirs(listOf("source/main/kotlin"))
            kotlin.destinationDirectory.set(file(programDir))
        }
    }
    test {
        kotlin {
            kotlin.setSrcDirs(listOf("source/test/kotlin"))
            kotlin.destinationDirectory.set(file(testsDir))
        }
    }
}

// Set Kotlin compiler settings.
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(project.extra["minJavaVersion"] as String))
    }
}

// Configure Detekt static analysis.
detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("detekt.yml"))
    source.setFrom("source/main/kotlin", "source/test/kotlin")
}

// Configure Dokka HTML output directory.
dokka {
    dokkaPublications.html {
        outputDirectory.set(file(autoDocDir))
    }
    dokkaSourceSets.main {
        includes.from("OVERVIEW.md")
    }
}

// Set the application group.
group = project.extra["APP_GROUP"] as String
// Set the application version.
version = project.extra["APP_VERSION"] as String

// Default task to be executed if no tasks are specified at the command line.
defaultTasks("dist")

// Define custom build tasks.
tasks.register("checkEnv") {
    description = "Check the build pre-conditions."

    val javaVersion = JavaVersion.current()
    val minJavaVersion = project.java.targetCompatibility
    if (javaVersion < minJavaVersion) {
        throw GradleException("Inappropriate Java version ($javaVersion). " +
            "Needs ($minJavaVersion) or higher.")
    }

    val gradleVersion = GradleVersion.version(project.gradle.gradleVersion)
    val minGradleVersion = GradleVersion.version(project.extra["minGradleVersion"] as String)
    if (gradleVersion < minGradleVersion) {
        throw GradleException("Inappropriate Gradle version ($gradleVersion)." +
            " Needs ($minGradleVersion) or higher.")
    }
}

tasks.register("dist") {
    description = "Create project distribution."

    // Ensure ordered execution of dependent tasks (this is workaround for Gradle design weakness).
    val tList = listOf("checkEnv", "build", "generateDocs", "publish")
        .stream().map { t -> tasks[t] }.toList()
    for (i in 0 until tList.size - 1) {
        tList[i + 1].mustRunAfter(tList[i])
    }
    dependsOn(tList)
}

tasks.register<Jar>("createSourcesJar") {
    description = "Create project sources Jar file."
    archiveBaseName.set(project.extra["APP_SHORT_NAME"] as String)

    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    manifest {
        attributes["Specification-Title"]   = "${project.extra["APP_NAME"]} Sources"
        attributes["Specification-Version"] = project.extra["APP_VERSION"]
        attributes["Specification-Vendor"]  = project.extra["APP_MANUFACTURER"]
    }
}

tasks.register<Jar>("createJavadocJar") {
    description = "Create project Javadoc Jar file."
    dependsOn("dokkaGeneratePublicationHtml")
    archiveBaseName.set(project.extra["APP_SHORT_NAME"] as String)

    archiveClassifier.set("javadoc")
    from(autoDocDir)
    manifest {
        attributes["Specification-Title"]   = "${project.extra["APP_NAME"]} Javadoc"
        attributes["Specification-Version"] = project.extra["APP_VERSION"]
        attributes["Specification-Vendor"]  = project.extra["APP_MANUFACTURER"]
    }
}

tasks.register("generateDocs") {
    description = "Generate project documentation."
    dependsOn("dokkaGeneratePublicationHtml")
}

tasks.register<Jar>("createJar") {
    description = "Create project Jar file."
    archiveBaseName.set(project.extra["APP_SHORT_NAME"] as String)

    from(sourceSets.main.get().output)
    manifest {
        attributes["Specification-Title"] = project.extra["APP_NAME"]
        attributes["Specification-Version"] = project.extra["APP_VERSION"]
        attributes["Specification-Vendor"] = project.extra["APP_MANUFACTURER"]
    }
}

// Customize project build tasks.
tasks {
    clean {
        // Clean the compilation target directories. Avoid leftovers from previous builds.
        delete(rootProject.layout.buildDirectory.get())
        delete(programDir)
        delete(testsDir)
        delete(logDir)
        delete(tempDir)
    }

    build {
        // Check build tools versions first.
        dependsOn("checkEnv")
    }

    test {
        // Workaround for test executions from Intellij Idea IDE.
        useJUnitPlatform()
    }

    jar {
        enabled = false // Do not produce default jar file for the project.
    }

    // Make Intellij Idea IDE to use the same build directories as Gradle.
    idea {
        module {
            outputDir = file(programDir)
            testOutputDir = file(testsDir)
            excludeDirs.addAll(setOf(file(logDir), file(tempDir)))
        }
    }
}

// Customize project artifacts publishing.
publishing {
    repositories {
        // Use local repository for the own libraries (not available in the Maven repo).
        val ownRepo = System.getProperty("user.home") + "/.gradle/local"
        ivy {
            url = uri(ownRepo)
        }
    }
    publications {
        // Generate project artifact in local Gradle repository.
        create<IvyPublication>("ivyJar") {
            organisation = project.group.toString()
            module = project.extra["APP_SHORT_NAME"] as String
            artifact(tasks["createJar"])
        }
        // Generate artifacts for Maven Central public repository.
        create<MavenPublication>("mavenJar") {
            groupId    = project.group.toString()
            artifactId = project.extra["APP_SHORT_NAME"] as String
            version    = project.extra["APP_VERSION"] as String
            artifact(tasks["createJar"])
            artifact(tasks["createSourcesJar"])
            artifact(tasks["createJavadocJar"])
            pom {
                name.set(project.extra["APP_NAME"] as String)
                description.set(project.extra["APP_DESCRIPTION"] as String)
                url.set("https://github.com/dimi2/Aplino")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set(project.extra["APP_MANUFACTURER"] as String)
                        name.set(project.extra["APP_MANUFACTURER"] as String)
                        url.set("https://github.com/dimi2")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/dimi2/Aplino.git")
                    developerConnection.set("scm:git:ssh://git@github.com/dimi2/Aplino.git")
                    url.set("https://github.com/dimi2/Aplino")
                }
            }
        }
    }
}

// Publish signed artifacts to Maven Central via Sonatype Central Portal.
nmcp {
    publishAllPublicationsToCentralPortal {
        username = findProperty("mavenCentralUsername") as String? ?: ""
        password = findProperty("mavenCentralPassword") as String? ?: ""
        publishingType = "USER_MANAGED"
    }
}

// Sign the published project artifacts.
signing {
    val signingKeyId = findProperty("signing.keyId") as String?
    val signingKeyFile = findProperty("signing.keyFile") as String?
    val signingKey = signingKeyFile?.let { file(it).readText() }
    val signingPassword = findProperty("signing.keyPassword") as String?
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["mavenJar"])
}
