pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "1.9.22" apply false
        kotlin("plugin.serialization") version "1.9.22" apply false
        id("com.github.johnrengelman.shadow") version "7.0.0" apply false
        application
    }
}

rootProject.name = "Laba"
include(":common", ":client", ":server")