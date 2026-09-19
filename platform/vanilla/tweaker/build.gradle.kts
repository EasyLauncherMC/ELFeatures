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
    implementation(project(":core"))

    api(project(":platform:vanilla:loader"))

    compileOnlyApi("net.minecraft:launchwrapper:1.12")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
