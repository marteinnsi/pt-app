import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

group = "marteinn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("com.varabyte.kotter:kotter-jvm:1.1.0")
    implementation("com.github.kotlin-inquirer:kotlin-inquirer:0.1.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("marteinn.MainKt")
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "marteinn.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}

//tasks {
//    withType<Jar> {
//        manifest {
//            attributes["Main-Class"] = application.mainClassName
//        }
//        // here zip stuff found in runtimeClasspath:
//        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//    }
//}