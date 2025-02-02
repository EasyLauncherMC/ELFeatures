plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared:asm"))

    implementation("org.ow2.asm:asm-all:5.0.3")
    implementation("org.apache.logging.log4j:log4j-api:2.0-beta9")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}