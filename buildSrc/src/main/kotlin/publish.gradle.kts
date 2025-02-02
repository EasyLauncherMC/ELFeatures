import elfeatures.gradle.model.Mod
import java.nio.file.Path
import java.util.stream.Collectors

plugins {
    java
    `maven-publish`
    signing
    id("tech.yanand.maven-central-publish")
}

// load Mod info
val mod: Mod = rootProject.extra["mod"] as Mod

val projectDir: Path = project.layout.projectDirectory.asFile.toPath()
val rootProjectDir: Path = rootProject.layout.projectDirectory.asFile.toPath()

val artifactIdSuffix: String = rootProjectDir.relativize(projectDir).toString().split(File.separator).stream()
    .filter { name -> name != "platform" }
    .collect(Collectors.joining("-"))


tasks.register<Jar>("javadocJar") {
    archiveAppendix = artifactIdSuffix
    archiveBaseName = mod.id
    archiveClassifier = "javadoc"
    destinationDirectory = layout.buildDirectory.dir("libs")
}

tasks.register<Jar>("sourcesJar") {
    archiveAppendix = artifactIdSuffix
    archiveBaseName = mod.id
    archiveClassifier = "sources"
    destinationDirectory = layout.buildDirectory.dir("libs")
}

// configure publishing to Maven Central
mavenCentral {
    authToken = System.getenv("PUBLISH_AUTH_TOKEN")?:"unknown"
}

// configure publications
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "${mod.id}-$artifactIdSuffix"
            version = project.version.toString()

            artifact(tasks.named<Jar>("javadocJar"))
            artifact(tasks.named<Jar>("sourcesJar"))

            pom {
                name = rootProject.name
                description = mod.description
                url = mod.sources

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/EasyLauncherMC/ELFeatures/blob/main/LICENSE"
                    }
                }

                developers {
                    developer {
                        name = "SoKnight"
                        email = "support@easylauncher.org"
                        organization = "EasyLauncher"
                        organizationUrl = "https://easylauncher.org/"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/EasyLauncherMC/ELFeatures.git"
                    developerConnection = "scm:git:ssh://github.com:EasyLauncherMC/ELFeatures.git"
                    url = "https://github.com/EasyLauncherMC/ELFeatures/tree/main"
                }
            }
        }
    }
}

// configure signing
signing {
    sign(publishing.publications["maven"])
}