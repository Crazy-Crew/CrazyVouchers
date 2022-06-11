plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

rootProject.group = "me.badbones69"
rootProject.version = "1.9.2"

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
        archiveFileName.set("${rootProject.name}[v${rootProject.version}].jar")

        relocate("de.tr7zw", "me.badbones69.libs.nbtapi")
        relocate("org.bstats", "me.badbones69.libs.bstats")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                "version" to rootProject.version
            )
        }
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"

    targetCompatibility = "8"
    sourceCompatibility = "8"
}