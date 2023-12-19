plugins {
    `java-library`
}

val specialVersion = "3.2.1"

rootProject.version = if (System.getenv("BUILD_NUMBER") != null) "$specialVersion-${System.getenv("BUILD_NUMBER")}" else specialVersion

tasks {
    assemble {
        val jarsDir = File("$rootDir/jars")

        doFirst {
            delete(jarsDir)

            jarsDir.mkdirs()
        }

        subprojects.forEach { project ->
            dependsOn(":${project.name}:build")

            doLast {
                runCatching {
                    copy {
                        from(project.layout.buildDirectory.file("libs/${rootProject.name}-${rootProject.version}.jar"))
                        into(jarsDir)
                    }
                }.onSuccess {
                    // Delete to save space on jenkins.
                    //delete(project.layout.buildDirectory.get())
                    //delete(rootProject.layout.buildDirectory.get())
                }.onFailure {
                    println("Failed to copy file out of build folder into jars directory: Likely does not exist.")
                }
            }
        }
    }

    subprojects {
        apply(plugin = "java-library")

        repositories {
            maven("https://repo.crazycrew.us/releases")

            maven("https://jitpack.io/")

            mavenCentral()
        }

        if (name == "paper") {
            repositories {
                maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

                maven("https://repo.codemc.io/repository/maven-public/")

                maven("https://repo.triumphteam.dev/snapshots/")

                maven("https://repo.oraxen.com/releases/")

                flatDir { dirs("libs") }
            }
        }

        tasks {
            compileJava {
                options.encoding = Charsets.UTF_8.name()
                options.release.set(17)
            }

            javadoc {
                options.encoding = Charsets.UTF_8.name()
            }

            processResources {
                filteringCharset = Charsets.UTF_8.name()
            }
        }

        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }
}