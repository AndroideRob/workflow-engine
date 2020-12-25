import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion by extra("1.3.1")

plugins {
    application
    kotlin("jvm") version "1.4.10"
    id("com.google.cloud.tools.jib") version "1.6.1"
}

group = "workflowapp"
version = "0.0.1"
val mainClass by extra("io.ktor.server.netty.EngineMain")

application {
    mainClassName = mainClass

    applicationDefaultJvmArgs = listOf(
        "-server",
        "-Djava.awt.headless=true",
        "-Xms128m",
        "-Xmx256m",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=100"
    )
}

dependencies {
    implementation(kotlin("stdlib"))

    // library
    implementation(project(":workflow"))
    // implementation("com.AndroideRob.workflow-engine:workflow:0.0.2")

    // ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-metrics:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs =
        listOf(*kotlinOptions.freeCompilerArgs.toTypedArray(), "-Xjvm-default=all")
}

jib {
    to {
        image = "robkonarski/workflowapp:$version"
    }
    from {
        image = "anapsix/alpine-java:latest"
    }
    container {
        ports = listOf("80")
        mainClass = this@Build_gradle.mainClass
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://kotlin.bintray.com/ktor")

//    maven("https://maven.pkg.github.com/AndroideRob/workflow-engine") {
//        name = "workflow-engine"
//        credentials {
//            username = "AndroideRob"
//            password = "e5ee14408998cb29efda02de7e131bc9f965a0fa"
//        }
//    }
}
