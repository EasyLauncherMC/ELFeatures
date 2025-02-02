plugins {
    java
    `base-modlocator`
    publish
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

repositories {
    maven("https://maven.minecraftforge.net/")
    mavenCentral()
}

dependencies {
    implementation(project(":facade:forgespi"))     // net.minecraftforge:forgespi:4.0.10

    compileOnly("org.apache.logging.log4j:log4j-api:2.15.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}