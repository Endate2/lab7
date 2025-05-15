plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.postgresql:postgresql:42.6.0") // или последняя версия
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.charleskorn.kaml:kaml:0.51.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("ServerKt")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("server")
    archiveClassifier.set("")
    archiveVersion.set("")


    dependsOn(tasks.jar)
    from(sourceSets.main.get().output)


    configurations = listOf(project.configurations.runtimeClasspath.get())


}


tasks.named("distZip") {
    dependsOn("shadowJar")
}
tasks.named("distTar") {
    dependsOn("shadowJar")
}
tasks.named("startScripts") {
    dependsOn("shadowJar")
}