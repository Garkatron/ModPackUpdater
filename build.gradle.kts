plugins {
    kotlin("jvm") version "1.9.10" // Versión estable y bien soportada de Kotlin
    application
}

group = "deus.mpu"
version = project.version

application {
    mainClass.set("deus.MainKt") // Replace with your package and main class name
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // Versión estable de OkHttp
    implementation("com.squareup.okio:okio:3.2.0")     // Versión que es compatible con Kotlin 1.9.x
}

java {
    sourceCompatibility = JavaVersion.VERSION_17  // Utiliza la versión LTS de Java para mayor compatibilidad
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17) // Configura el JDK 17 para mayor compatibilidad
}
