plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation(project(":core"))

    api("io.github.llamalad7:mixinextras-common:0.5.5")
    api("net.fabricmc:mapping-io:0.7.1")
    api("net.fabricmc:sponge-mixin:0.17.4+mixin.0.8.7")

    api(libs.asm.commons)
    api(libs.asm.util)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
