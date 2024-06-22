import com.ryderbelserion.feather.tools.formatLog
import com.ryderbelserion.feather.tools.latestCommitHash
import com.ryderbelserion.feather.tools.latestCommitMessage

plugins {
    alias(libs.plugins.minotaur)
    alias(libs.plugins.hangar)

    `java-plugin`
}

val buildNumber: String? = System.getenv("BUILD_NUMBER")

rootProject.version = if (buildNumber != null) "${libs.versions.minecraft.get()}-$buildNumber" else "3.6.1"

val isSnapshot = false

val content: String = if (isSnapshot) {
    formatLog(latestCommitHash(), latestCommitMessage(), rootProject.name, "Crazy-Crew")
} else {
    rootProject.file("CHANGELOG.md").readText(Charsets.UTF_8)
}

subprojects.filter { it.name != "api" }.forEach {
    it.project.version = rootProject.version
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))

    projectId.set(rootProject.name.lowercase())

    versionType.set(if (isSnapshot) "beta" else "release")

    versionName.set("${rootProject.name} ${rootProject.version}")
    versionNumber.set(rootProject.version as String)

    changelog.set(content)

    uploadFile.set(rootProject.projectDir.resolve("jars/${rootProject.name}-${rootProject.version}.jar"))

    syncBodyFrom.set(rootProject.file("README.md").readText(Charsets.UTF_8))

    gameVersions.add(libs.versions.minecraft.get())

    loaders.addAll(listOf("purpur", "paper", "folia"))

    autoAddDependsOn.set(false)
    detectLoaders.set(false)
}

hangarPublish {
    publications.register("plugin") {
        apiKey.set(System.getenv("HANGAR_KEY"))

        id.set(rootProject.name.lowercase())

        version.set(rootProject.version as String)

        channel.set(if (isSnapshot) "Snapshot" else "Release")

        changelog.set(content)

        platforms {
            paper {
                jar.set(rootProject.projectDir.resolve("jars/${rootProject.name}-${rootProject.version}.jar"))

                platformVersions.set(listOf(libs.versions.minecraft.get()))

                dependencies {
                    hangar("PlaceholderAPI") {
                        required = false
                    }

                    url("Oraxen", "https://www.spigotmc.org/resources/%E2%98%84%EF%B8%8F-oraxen-custom-items-blocks-emotes-furniture-resourcepack-and-gui-1-18-1-20-4.72448/") {
                        required = false
                    }
                }
            }
        }
    }
}
