import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logbackVersion by extra("1.2.3")
val exposedVersion by extra("0.28.1")
val postgresDriverVersion by extra("42.2.12")
val mysqlDriverVersion by extra("8.0.15")
val h2DriverVersion by extra("1.4.200")
val hikariVersion by extra("3.4.5")
val caffeineVersion by extra("2.8.5")
val commonsVersion by extra("3.11")
val kryoVersion by extra("5.0.0")

val junitVersion by extra("4.13")
val mockkVersion by extra("1.10.2")

plugins {
    kotlin("jvm")
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(kotlin("stdlib"))

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // database
    implementation("org.postgresql:postgresql:$postgresDriverVersion")
    implementation("mysql:mysql-connector-java:$mysqlDriverVersion")
    implementation("com.h2database:h2:$h2DriverVersion")
    api("com.zaxxer:HikariCP:$hikariVersion")

    // orm
    api("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("org.apache.commons:commons-lang3:$commonsVersion")
    implementation("com.esotericsoftware.kryo:kryo5:$kryoVersion")

    testImplementation("junit:junit:$junitVersion")
    testImplementation("io.mockk:mockk:${mockkVersion}")
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs =
        listOf(*kotlinOptions.freeCompilerArgs.toTypedArray(), "-Xjvm-default=enable")
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://kotlin.bintray.com/ktor")
}

publishing {
    repositories {
        maven {
            name = "workflow-engine"
            url = uri("https://maven.pkg.github.com/AndroideRob/workflow-engine")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}
