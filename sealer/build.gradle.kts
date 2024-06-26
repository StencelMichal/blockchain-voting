import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    id("java")
    kotlin("plugin.spring") version "1.9.22"
//    id("org.web3j") version "4.11.0"
    kotlin("plugin.noarg") version "1.9.23"
}

group = "com.stencel.evoting"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven(url = "https://artifacts.consensys.net/public/maven/maven/")
}

dependencies {

    implementation(project(":smartContracts"))

    // web3j
    implementation("org.web3j:core:4.11.0")
    implementation("org.web3j:abi:4.11.0")

//    implementation("org.bouncycastle:bcprov-jdk16:1.46")

    // pailier ecrpytion
    implementation("com.n1analytics:javallier_2.10:0.6.0")

    //lombok
    compileOnly("org.projectlombok:lombok:1.18.32")

    // spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    // serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

noArg {
    annotation("jakarta.persistence.Entity")
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
