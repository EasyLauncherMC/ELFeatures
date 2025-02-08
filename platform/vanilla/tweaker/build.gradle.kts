plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":shared:mixin"))

    implementation("net.fabricmc:mapping-io:0.7.1")
    implementation("net.fabricmc:sponge-mixin:0.15.0+mixin.0.8.7")

    compileOnly("net.minecraft:launchwrapper:1.12")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}