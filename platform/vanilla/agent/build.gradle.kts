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

    api(project(":platform:vanilla:loader"))

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
