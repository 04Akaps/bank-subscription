plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"

    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("org.springframework.boot") version "3.2.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // kafka
    implementation("org.springframework.kafka:spring-kafka:3.1.0")

    // Jackson 의존성 추가
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}