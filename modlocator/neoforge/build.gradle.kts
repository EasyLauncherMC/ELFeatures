plugins {
    java
    `base-modlocator`
    publish
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    maven("https://maven.neoforged.net/releases/")
    mavenCentral()
}

dependencies {
    implementation(project(":facade:fml_loader"))   // net.neoforged.fancymodloader:loader:4.0.6
    implementation(project(":facade:fml_spi"))      // net.neoforged.fancymodloader:spi:2.0.17
    implementation(project(":facade:neoforgespi"))  // net.neoforged:neoforgespi:8.0.0

    compileOnly("org.apache.logging.log4j:log4j-api:2.15.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}