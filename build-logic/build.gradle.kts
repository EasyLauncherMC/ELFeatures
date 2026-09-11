plugins {
    `kotlin-dsl`
}

group = "elfeatures"
version = "1.0.0"

repositories {
    gradlePluginPortal()
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    implementation("net.minecraftforge.gradle:net.minecraftforge.gradle.gradle.plugin:7.0.40")
    implementation("net.minecraftforge.jarjar:net.minecraftforge.jarjar.gradle.plugin:0.2.3")
    implementation("net.minecraftforge.renamer:net.minecraftforge.renamer.gradle.plugin:1.1.0")
    implementation("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.6.1")
    implementation("tech.yanand.maven-central-publish:tech.yanand.maven-central-publish.gradle.plugin:1.3.0")

    // every plugin of the build sees ASM from here first, and left alone it resolves to a 9.2 that can't read
    // the Java 21 class files Ornithe's feather constants are compiled to
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
    implementation("org.ow2.asm:asm-util:9.10.1")

    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("commons-io:commons-io:2.21.0")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
}

gradlePlugin {
    plugins {
        create("elfeatures") {
            id = "elfeatures"
            implementationClass = "elfeatures.gradle.BuildPlugin"
        }
    }
}