plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "22"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("application.App")
}

sourceSets {
    val main by getting {
        java.srcDirs("src")
        resources.srcDirs("res")
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r")
    implementation("com.opencsv:opencsv:5.7.1")
    implementation("com.jfoenix:jfoenix:9.0.10")
}

// Disable tests if none exist
tasks.named("test") {
    enabled = false
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "application.App"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    // 🚨 Exclude signature files that break the jar
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

