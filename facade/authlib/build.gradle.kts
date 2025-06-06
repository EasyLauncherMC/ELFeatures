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

    compileOnlyApi(libs.lombok)
    annotationProcessor(libs.lombok)
}