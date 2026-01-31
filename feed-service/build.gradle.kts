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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // 1. CORE WEB (API banane ke liye)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 2. DATABASE & ORM (Postgres + Hibernate)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql") // Driver

    // 3. SECURITY & AUTH (JWT Logic)
    implementation("org.springframework.boot:spring-boot-starter-security")
    // JWT ke liye JJWT library (Latest stable version use kar)
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // 4. KOTLIN MAGIC (Data Classes & JSON)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // JSON parsing ke liye critical
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Spring ko Kotlin classes padhne ke liye chahiye
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // 5. VALIDATION (Inputs check karne ke liye - @NotNull, @Email, etc.)
    // Ye bohot zaruri hai taaki user ganda data na bheje
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 6. DEV TOOLS (Optional: Fast reload ke liye)
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // 7. TESTING (Junit 5 built-in hota hai usually)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
