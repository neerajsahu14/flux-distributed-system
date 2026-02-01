plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.neerajsahu.flux.server"
version = "0.0.1-SNAPSHOT"
description = "feed-service"

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

repositories {
    mavenCentral()
}

dependencies {
    dependencies {
        // 1. CORE WEB (for building APIs)
        implementation("org.springframework.boot:spring-boot-starter-web")

        // 2. DATABASE & ORM (Postgres + Hibernate)
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        runtimeOnly("org.postgresql:postgresql") // Driver

        // 3. SECURITY & AUTH (JWT logic)
        implementation("org.springframework.boot:spring-boot-starter-security")
        // JJWT library for JWTs
        implementation("io.jsonwebtoken:jjwt-api:0.13.0")
        runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
        runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

        // 4. KOTLIN (Data classes & JSON)
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // critical for JSON parsing
        implementation("org.jetbrains.kotlin:kotlin-reflect") // required for Spring to read Kotlin classes
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        // 5. VALIDATION (input checks - @NotNull, @Email, etc.)
        // Important to prevent invalid user data
        implementation("org.springframework.boot:spring-boot-starter-validation")

        // 6. DEV TOOLS (optional: fast reload)
        developmentOnly("org.springframework.boot:spring-boot-devtools")

        // 7. TESTING (JUnit 5 is usually included)
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.security:spring-security-test")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
