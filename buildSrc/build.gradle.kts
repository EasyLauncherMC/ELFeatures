plugins {
    java
    `kotlin-dsl`
}

group = "elfeatures"
version = "1.0.0"

repositories {
    gradlePluginPortal()
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    implementation("net.minecraftforge.gradle:ForgeGradle:6.+")
    implementation("net.neoforged.gradle:userdev:7.0.180")
    implementation("net.neoforged.gradle:mixin:7.0.180")
    implementation("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    implementation("net.fabricmc:fabric-loom:1.7-SNAPSHOT")
    implementation("com.github.johnrengelman:shadow:8.1.1")

    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("org.apache.commons:commons-lang3:3.15.0")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("commons-io:commons-io:2.16.1")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}