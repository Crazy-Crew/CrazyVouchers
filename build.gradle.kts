plugins {
    `maven-publish`
    `java-library`
}

defaultTasks("build")

rootProject.group = "com.badbones69.crazyvouchers"
rootProject.description = "Want to make a paper that can give you an axolotl with a pretty firework display, Look no further!"
rootProject.version = "3.0.1"

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")

    repositories {
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

        maven("https://repo.codemc.io/repository/maven-public/")

        maven("https://jitpack.io/")

        mavenCentral()
    }

    listOf(
        ":paper"
    ).forEach {
        project(it) {
            group = "${rootProject.group}.${this.name}"
            version = rootProject.version
        }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of("17"))
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.release.set(17)
        }
    }

    val isSnapshot = rootProject.version.toString().contains("snapshot")

    publishing {
        repositories {
            maven {
                credentials {
                    this.username = System.getenv("gradle_username")
                    this.password = System.getenv("gradle_password")
                }

                if (isSnapshot) {
                    url = uri("https://repo.crazycrew.us/snapshots/")
                    return@maven
                }

                url = uri("https://repo.crazycrew.us/releases/")
            }
        }
    }
}

tasks {
    assemble {
        val jarsDir = File("$rootDir/jars")
        if (jarsDir.exists()) jarsDir.delete()

        subprojects.forEach { project ->
            dependsOn(":${project.name}:build")

            doLast {
                if (!jarsDir.exists()) jarsDir.mkdirs()

                val file = file("${project.buildDir}/libs/${rootProject.name}-${project.version}.jar")

                copy {
                    from(file)
                    into(jarsDir)
                }
            }
        }
    }
}