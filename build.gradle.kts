plugins {
    java

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val buildNumber: String? = System.getenv("BUILD_NUMBER")

val jenkinsVersion = "2.9.12-b$buildNumber"

group = "com.badbones69.vouchers"
version = "2.9.12"
description = "Want to make a paper that can give you an axolotl with a pretty firework display, Look no further! "

repositories {

    /**
     * Placeholders
     */
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    /**
     * Spigot Team
     */
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    /**
     * NBT API
     */
    maven("https://repo.codemc.org/repository/maven-public/")

    /**
     * Everything else we need.
     */
    mavenCentral()
}

dependencies {
    implementation("de.tr7zw", "nbt-data-api", "2.10.0")

    implementation("org.bstats", "bstats-bukkit", "3.0.0")

    compileOnly("org.spigotmc", "spigot-api", "1.19.2-R0.1-SNAPSHOT")

    compileOnly("me.clip", "placeholderapi", "2.11.2") {
        exclude(group = "org.spigotmc")
        exclude(group = "org.bukkit")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    shadowJar {
        if (buildNumber != null) {
            archiveFileName.set("${rootProject.name}-[v${jenkinsVersion}].jar")
        } else {
            archiveFileName.set("${rootProject.name}-[v${rootProject.version}].jar")
        }

        listOf(
            "de.tr7zw",
            "org.bstats"
        ).forEach {
            relocate(it, "${rootProject.group}.plugin.lib.$it")
        }
    }

    compileJava {
        options.release.set(17)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to rootProject.name,
                "group" to rootProject.group,
                "version" to rootProject.version,
                "description" to rootProject.description
            )
        }
    }
}