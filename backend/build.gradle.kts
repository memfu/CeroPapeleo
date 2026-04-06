plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
    id("io.ktor.plugin") version "3.0.0"
    // AÑADIDO: Plugin para generar el archivo ejecutable para AWS
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.ceropapeleo.backend"
version = "1.0.0"

dependencies {
    // Logs (con parches de seguridad)
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("ch.qos.logback:logback-core:1.5.12")
    constraints {
        implementation("ch.qos.logback:logback-core:1.5.12") {
            because("Corrige vulnerabilidades críticas y es la versión estable más reciente en Maven Central")
        }
    }

    // Núcleo de Ktor y Servidor Netty
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")

    // JSON y Serialización
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // PARCHE DE SEGURIDAD: GSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Motor de generación del PDF
    implementation("org.apache.pdfbox:pdfbox:3.0.1")

    // Comunicación y Asincronía
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // PARCHE DE SEGURIDAD: Motor de red
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okio:okio:3.6.0")

    // SDK de AWS para Kotlin
    implementation("aws.sdk.kotlin:s3:1.0.0")
    implementation("aws.sdk.kotlin:dynamodb:1.0.0")
}

application {
    // Ruta confirmada según tu paquete
    mainClass.set("com.ceropapeleo.backend.ApplicationKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    targetCompatibility = "21"
    sourceCompatibility = "21"
}

// AÑADIDO: Configuración específica para crear el JAR de Elastic Beanstalk
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("ceropapeleo-backend-all.jar")
    manifest {
        attributes["Main-Class"] = "com.ceropapeleo.backend.ApplicationKt"
    }
}