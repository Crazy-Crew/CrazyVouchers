plugins {
    java
    idea

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.badbones69.vouchers"
version = "2.9.10-SNAPSHOT"
description = "Make Custom Vouchers just for your server!"

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

repositories {
    mavenCentral()

    maven("https://repo.codemc.org/repository/maven-public/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.1-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.10.10")

    implementation("de.tr7zw:nbt-data-api:2.10.0")

    implementation("org.bstats:bstats-bukkit:3.0.0")
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}-[1.18-1.19]-[v${rootProject.version}].jar")

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
