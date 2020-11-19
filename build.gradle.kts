import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    kotlin("plugin.noarg") version "1.4.10"
    application
}
group = "me.toni"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/ktor")
    maven("https://dl.bintray.com/kotlin/kotlinx")
}
dependencies {
    fun ktor(module: String) = "io.ktor:ktor-$module:1.4.0"

    testImplementation(kotlin("test-junit"))
    testImplementation("com.github.stefanbirkner:system-rules:1.19.0")
    testImplementation("com.github.javafaker:javafaker:1.0.2")

    implementation(ktor("server-netty"))
    implementation(ktor("html-builder"))
    implementation(ktor("auth-jwt"))
    implementation(ktor("serialization"))
    testImplementation(ktor("server-test-host"))
    implementation(ktor("client-cio"))
    implementation(ktor("websockets"))

    implementation("org.neo4j:neo4j-ogm-core:3.2.17")
    runtimeOnly("org.neo4j:neo4j-ogm-bolt-driver:3.2.17")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
application {
    mainClassName = "MainKt"
}

noArg {
    annotation("org.neo4j.ogm.annotation.RelationshipEntity")
    annotation("org.neo4j.ogm.annotation.NodeEntity")
}