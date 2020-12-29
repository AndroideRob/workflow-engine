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
    id("com.jfrog.bintray") version "1.8.4"
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

val artifactGroup = "com.robkonarski.workflow"
val artifactName = "engine"
val artifactVersion = "0.0.5"
val artifactRepo = "workflow-engine"

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

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
    publications {
        register<MavenPublication>(artifactRepo) {
            from(components["java"])
            artifact(sourcesJar)
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
        }
    }
}

bintray {
    user = project.findProperty("bintray.user") as String? ?: System.getenv("BINTRAY_USER")
    key = project.findProperty("bintray.key") as String? ?: System.getenv("BINTRAY_API_KEY")
    setPublications(artifactRepo)
    publish = true

    pkg.apply {
        repo = artifactRepo
        name = artifactName
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/AndroideRob/workflow-engine"
        version.apply {
            name = artifactVersion
        }
    }
}
