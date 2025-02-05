import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    `maven-publish`
    signing
    id("tech.yanand.maven-central-publish")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

java {
    toolchain.languageVersion = JavaLanguageVersion.of(spec.javaVersion)
}

mavenCentral {
    authToken = System.getenv("PUBLISH_AUTH_TOKEN")?:"unknown"
}

tasks.register<Jar>("javadocJarStub") {
    archiveClassifier = "javadoc"
    destinationDirectory = tasks.jar.get().destinationDirectory
}

tasks.register<Jar>("sourcesJarStub") {
    archiveClassifier = "sources"
    destinationDirectory = tasks.jar.get().destinationDirectory
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = base.archivesName.get()
            version = project.version.toString()

            artifact(spec.publishJarTask) {
                classifier = null
            }

            artifact(tasks["javadocJarStub"])
            artifact(tasks["sourcesJarStub"])

            pom {
                name = rootProject.name
                description = spec.mod.description
                url = spec.mod.sources

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

signing {
    sign(publishing.publications["maven"])
}