plugins {
    // Hereda la versión de Kotlin del proyecto raíz (CeroPapeleo)
    kotlin("jvm")

    // Definimos explícitamente la serialización con su ID completo
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"

    // Plugin de Ktor para empaquetar y ejecutar el servidor
    id("io.ktor.plugin") version "3.0.0"
}

group = "com.ceropapeleo.backend"
version = "1.0.0"

dependencies {
    // Núcleo de Ktor y Servidor Netty
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // JSON y Serialización (Para hablar con la App Android)
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // Logs (Para ver qué pasa en la consola)
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("ch.qos.logback:logback-classic:1.4.12")
}

application {
    // Esta ruta debe coincidir exactamente con el paquete de tu Application.kt
    mainClass.set("com.ceropapeleo.backend.ApplicationKt")
}

kotlin {
    jvmToolchain(21) // Esto obliga a Kotlin a usar Java 21
}

tasks.withType<JavaCompile> {
    targetCompatibility = "21" // Esto obliga a Java a usar Java 21
    sourceCompatibility = "21"
}