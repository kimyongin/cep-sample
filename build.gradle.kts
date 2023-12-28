plugins {
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.2"
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.wso2.org/nexus/content/repositories/releases/")
    }
    maven {
        url = uri("https://dist.wso2.org/maven2/")
    }
}

dependencies {
    implementation ("org.springframework.boot:spring-boot-starter")
    implementation ("org.springframework.boot:spring-boot-starter-logging")

    implementation ("io.siddhi:siddhi-core:5.1.28")
    implementation ("io.siddhi:siddhi-query-api:5.1.28")
    implementation ("io.siddhi:siddhi-query-compiler:5.1.28")
    implementation ("io.siddhi:siddhi-annotations:5.1.28")
    implementation ("org.springframework.kafka:spring-kafka:3.1.1")

    implementation ("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    implementation ("ch.qos.logback.contrib:logback-jackson:0.1.5")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.16.1")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    compileOnly ("org.projectlombok:lombok:1.18.30")
    annotationProcessor ("org.projectlombok:lombok:1.18.30")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}