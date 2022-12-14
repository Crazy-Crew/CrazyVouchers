plugins {
    `java-library`

    `maven-publish`

    id("com.modrinth.minotaur") version "2.5.0"

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

project.group = "com.badbones69.crazyvouchers"
project.version = "${extra["plugin_version"]}"
project.description = "Want to make a paper that can give you an axolotl with a pretty firework display, Look no further! "

repositories {
    /**
     * Placeholders
     */
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    /**
     * NBT API
     */
    maven("https://repo.codemc.org/repository/maven-public/")

    /**
     * Paper Team
     */
    maven("https://repo.papermc.io/repository/maven-public/")

    /**
     * Everything else we need.
     */
    mavenCentral()
}

dependencies {
    implementation("de.tr7zw", "nbt-data-api", "2.10.0")

    implementation("org.bstats", "bstats-bukkit", "3.0.0")

    compileOnly("io.papermc.paper", "paper-api", "${project.extra["minecraft_version"]}-R0.1-SNAPSHOT")

    compileOnly("me.clip", "placeholderapi", "2.11.2") {
        exclude(group = "org.spigotmc")
        exclude(group = "org.bukkit")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

val buildNumber: String? = System.getenv("BUILD_NUMBER")
val buildVersion = "${project.version}-b$buildNumber-SNAPSHOT"

tasks {
    shadowJar {
        if (buildNumber != null) {
            archiveFileName.set("${rootProject.name}-${buildVersion}.jar")
        } else {
            archiveFileName.set("${rootProject.name}-${project.version}.jar")
        }

        listOf(
            "de.tr7zw",
            "org.bstats"
        ).forEach {
            relocate(it, "${rootProject.group}.plugin.lib.$it")
        }
    }

    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("crazyvouchers")
        versionName.set("${rootProject.name} ${project.version} Update")
        versionNumber.set("${project.version}")
        versionType.set("${extra["version_type"]}")
        uploadFile.set(shadowJar.get())

        autoAddDependsOn.set(true)

        gameVersions.addAll(listOf("1.19", "1.19.1", "1.19.2", "1.19.3"))
        loaders.addAll(listOf("paper", "purpur"))

        changelog.set(System.getenv("COMMIT_MESSAGE"))
    }

    compileJava {
        options.release.set(17)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to rootProject.name,
                "group" to project.group,
                "version" to if (buildNumber != null) buildVersion else project.version,
                "description" to project.description
            )
        }
    }
}


publishing {
    repositories {
        maven("https://repo.crazycrew.us/releases") {
            name = "crazycrew"
            credentials {
                username = System.getenv("CRAZYCREW_USERNAME")
                password = System.getenv("CRAZYCREW_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = rootProject.name.toLowerCase()
            version = "${project.version}"
            from(components["java"])
        }
    }
}