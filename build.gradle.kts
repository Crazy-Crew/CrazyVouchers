plugins {
    java

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.badbones69.vouchers"
version = "1.8-1.19-1.9.2"
description = "Make Custom Vouchers just for your server!"

repositories {
    mavenCentral()

    maven("https://repo.codemc.org/repository/maven-public/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.10.10")

    implementation("de.tr7zw:nbt-data-api:2.10.0-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.0")
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")

        listOf(
            "de.tr7zw",
            "org.bstats"
        ).forEach {
            relocate(it, "${rootProject.group}.plugin.lib.$it")
        }
    }

    compileJava {
        targetCompatibility = "8"
        sourceCompatibility = "8"
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to rootProject.name,
                "group" to project.group,
                "version" to project.version,
                "description" to project.description
            )
        }
    }
}