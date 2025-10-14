plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.adas"
version = "0.0.1-SNAPSHOT"
description = "api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Default
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    developmentOnly("org.springframework.boot:spring-boot-devtools")


    // Docker
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Test
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // OpenAPI/Swagger UI (Springdoc) — align with Spring Boot 3.5.x
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
