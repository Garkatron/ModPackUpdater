plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

repositories {
    mavenCentral() // Ensure dependencies are resolved from Maven Central
}

application {
    mainClass.set("deus.MainKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okio:okio:3.4.0")
    implementation("org.json:json:20231013")

}
// Configuración de la versión de Java
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "8"
    targetCompatibility = "8"
}

tasks.shadowJar {
    archiveClassifier.set("") // Remove the default '-all' suffix
    manifest {
        attributes["Main-Class"] = "deus.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
