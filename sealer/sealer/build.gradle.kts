import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("java")
//    id("org.web3j") version "4.11.0"
}

group = "com.stencel.evoting"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {

    // web3j
    implementation("org.web3j:core:5.0.0")
    implementation("org.web3j:abi:4.7.0")
//    implementation("org.web3j:codegen:5.0.0")
//    implementation("org.web3j:crypto:5.0.0")
//    implementation("org.web3j:utils:5.0.0")
//    implementation("org.web3j:rlp:5.0.0")



//    implementation("org.web3j:web3j-gradle-plugin:4.6.0")

    implementation("org.bouncycastle:bcprov-jdk16:1.46")



    // spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    // serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
