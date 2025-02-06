plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    maven("https://maven.minecraftforge.net/")
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    compileOnly("org.spongepowered:mixin:0.8.5")

    implementation("com.google.code.gson:gson:2.8.0")
    implementation("org.ow2.asm:asm-tree:9.1")
    implementation("org.apache.logging.log4j:log4j-api:2.0-beta9")

    compileOnlyApi("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}