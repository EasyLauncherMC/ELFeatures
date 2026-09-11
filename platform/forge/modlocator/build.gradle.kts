plugins {
    java
    `base-modlocator`
    publish
}

repositories {
    maven("https://maven.minecraftforge.net/")
    mavenCentral()
}

dependencies {
    implementation(project(":facade:forgespi"))     // net.minecraftforge:forgespi:4.0.10

    compileOnly("org.apache.logging.log4j:log4j-api:2.15.0")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}