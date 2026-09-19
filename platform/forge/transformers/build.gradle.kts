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
    compileOnly(project(":core"))
    implementation(project(":shared:asm"))

    implementation("org.ow2.asm:asm-all:5.0.3")

    annotationProcessor(libs.lombok)
}