import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.userdev)
    alias(libs.plugins.modrinth)
    alias(libs.plugins.hangar)
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.codemc.io/repository/maven-public/")

    maven("https://jitpack.io/")

    mavenCentral()
}

dependencies {
    implementation("de.tr7zw", "item-nbt-api", "2.11.3")
    implementation("org.bstats", "bstats-bukkit", "3.0.2")

    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    compileOnly("me.clip", "placeholderapi", "2.11.3")
}

val component: SoftwareComponent = components["java"]

tasks {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString()
                artifactId = "${rootProject.name.lowercase()}-api"
                version = rootProject.version.toString()

                from(component)
            }
        }
    }

    reobfJar {
        outputJar.set(file("$buildDir/libs/${rootProject.name}-${project.version}.jar"))
    }

    assemble {
        dependsOn(reobfJar)
    }

    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/**")

        listOf(
            "dev.triumphteam",
            "org.jetbrains",
            "org.bstats",
            "de.tr7zw"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        filesMatching("plugin.yml") {
            val props = mapOf(
                "name" to rootProject.name,
                "group" to rootProject.group,
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

val description = """
## Fix:
* Properly apply the damage to the items given to you when you right click the voucher
 * Change `Damage: 50` to `Damage:50` as it will not work with this.
    
## Other:
* [Feature Requests](https://github.com/Crazy-Crew/${rootProject.name}/discussions/categories/features)
* [Bug Reports](https://github.com/Crazy-Crew/${rootProject.name}/issues)
""".trimIndent()

val versions = listOf(
    "1.20",
    "1.20.1"
    //"1.20.2"
)

val output = file("${rootProject.rootDir}/jars/${rootProject.name}-${project.version}.jar")

val isSnapshot = rootProject.version.toString().contains("snapshot")
val type = if (isSnapshot) "beta" else "release"

modrinth {
    autoAddDependsOn.set(false)

    token.set(System.getenv("MODRINTH_TOKEN"))

    projectId.set(rootProject.name.lowercase())

    versionName.set("${rootProject.name} ${rootProject.version}")
    versionNumber.set("${rootProject.version}")

    uploadFile.set(output)

    gameVersions.addAll(versions)

    changelog.set(description)

    loaders.addAll("paper", "purpur")
}

hangarPublish {
    publications.register("plugin") {
        version.set(rootProject.version as String)
        namespace("CrazyCrew", rootProject.name)
        channel.set("Release")
        changelog.set(description)

        apiKey.set(System.getenv("hangar_key"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(output)
                platformVersions.set(versions)
            }
        }
    }
}