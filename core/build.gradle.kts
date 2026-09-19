plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("org.apache.logging.log4j:log4j-api:2.0-beta9")

    compileOnlyApi(libs.lombok)
    annotationProcessor(libs.lombok)
}
