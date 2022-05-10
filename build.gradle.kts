import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dit-calendar"
version = "0.10.0"

application {
    mainClass.set("com.ditcalendar.bot.BotKt")
    // Required by the 'shadowJar' task
    project.setProperty("mainClassName", "com.ditcalendar.bot.BotKt")
}

plugins {
    val kotlinVersion = "1.6.21"

    application

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    jcenter()
}

dependencies {
    val fuelVersion = "2.3.1"
    val kittinunfResultVersion = "5.2.1"
    val konfigVersion = "1.6.10.0"
    val kotlinxSerializationVersion = "1.3.2"
    val ktBotVersion = "1.4.1"
    val exposedVersion = "0.38.2"
    val postgresqlVersion = "42.2.2"

    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    compileOnly("com.github.kittinunf.fuel:fuel-kotlinx-serialization:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion")
    implementation("com.github.kittinunf.result:result:$kittinunfResultVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.github.elbekd:kt-telegram-bot:$ktBotVersion")
    implementation("com.natpryce:konfig:$konfigVersion")

    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.postgresql:postgresql:$postgresqlVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    sourceCompatibility = "11"
    kotlinOptions.jvmTarget = "11"

    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    incremental = true
}

tasks.register("stage") {
    dependsOn("build", "clean")
    mustRunAfter("clean")

    //clean up build
    doLast {
        File("build1").mkdirs()
        File("build/libs").copyRecursively(File("build1/libs"))
        delete("build")
        File("build1").renameTo(File("build"))
    }
}