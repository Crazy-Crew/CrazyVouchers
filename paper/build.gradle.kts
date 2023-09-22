import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    alias(libs.plugins.modrinth)
    alias(libs.plugins.hangar)

    id("paper-plugin")
}

project.group = "${rootProject.group}.paper"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation(project(":common"))

    implementation("org.bstats", "bstats-bukkit", "3.0.2")

    implementation("de.tr7zw", "item-nbt-api", "2.12.0")

    implementation(libs.cluster.bukkit.api) {
        exclude("com.ryderbelserion.cluster", "cluster-api")
    }

    compileOnly("me.clip", "placeholderapi", "2.11.3")

    compileOnly("com.github.LoneDev6", "API-ItemsAdder", "3.5.0b")

    compileOnly("com.github.oraxen", "oraxen", "1.160.0")
}

val component: SoftwareComponent = components["java"]

tasks {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = "${rootProject.name.lowercase()}-${project.name.lowercase()}-api"
                version = rootProject.version.toString()

                from(component)
            }
        }
    }

    shadowJar {
        listOf(
            "de.tr7zw.changeme.nbtapi",
            "dev.triumphteam",
            "org.jetbrains",
            "org.bstats"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        filesMatching("plugin.yml") {
            val props = mapOf(
                "name" to rootProject.name,
                "group" to project.group.toString(),
                "version" to rootProject.version,
                "description" to rootProject.description,
                "authors" to rootProject.properties["authors"],
                "apiVersion" to "1.20",
                "website" to "https://modrinth.com/plugin/${rootProject.name.lowercase()}"
            )

            expand(props)
        }
    }
}

val isSnapshot = rootProject.version.toString().contains("snapshot")
val type = if (isSnapshot) "beta" else "release"
val other = if (isSnapshot) "Beta" else "Release"

val file = file("${rootProject.rootDir}/jars/${rootProject.name}-${rootProject.version}.jar")

val description = """
## Changes:
* Added 1.20.2 support.
* Made sounds respect client side sound settings.
* Properly handle how Metrics shuts down and turns on when you change the true/false.
* Add more verbose logging with an option to turn off the spammy garbage.
* Internal placeholders like %random% have been changed to {random}, All internal usage's of the placeholders have been changed. I cannot change that for where you use the commands.
 * I do however still take those as valid input since I simply convert %random% to {random} under the hood but please change it, I won't be doing that forever though...
* Added volume/pitch controls to each sound section in the vouchers.

## Breaking Changes:
* Vouchers have been moved out of the Config.yml into a folder called `vouchers`
 * All the configurations should auto migrate. Let me know if I missed something.

## Performance:
* No longer use the player object in hashmap's/arrays just the uuid as god intended.

## Developers / API:
### This is nerd talk so only read this if you need to.
* Cleaned up some internals, reduce duplicated code.
    
## Other:
 * [Feature Requests](https://github.com/Crazy-Crew/${rootProject.name}/issues)
 * [Bug Reports](https://github.com/Crazy-Crew/${rootProject.name}/issues)
""".trimIndent()

val versions = listOf(
    "1.20",
    "1.20.1",
    "1.20.2"
)

modrinth {
    autoAddDependsOn.set(false)

    token.set(System.getenv("MODRINTH_TOKEN"))

    projectId.set(rootProject.name.lowercase())

    versionName.set("${rootProject.name} ${rootProject.version}")
    versionNumber.set("${rootProject.version}")

    versionType.set(type)

    uploadFile.set(file)

    gameVersions.addAll(versions)

    changelog.set(description)

    loaders.addAll("paper", "purpur")
}

hangarPublish {
    publications.register("plugin") {
        version.set(rootProject.version as String)

        id.set(rootProject.name)

        channel.set(if (isSnapshot) "Beta" else "Release")

        changelog.set(description)

        apiKey.set(System.getenv("hangar_key"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(file)
                platformVersions.set(versions)
            }
        }
    }
}