plugins {
    alias(libs.plugins.minotaur)
    alias(libs.plugins.feather)
    alias(libs.plugins.hangar)

    `config-java`
}

val git = feather.getGit()

val commitHash: String = git.getCurrentCommitHash().subSequence(0, 7).toString()
val isSnapshot: Boolean = git.getCurrentBranch() == "dev"
val content: String = if (isSnapshot) "[$commitHash](https://github.com/Crazy-Crew/${rootProject.name}/commit/$commitHash) ${git.getCurrentCommit()}" else rootProject.file("changelog.md").readText(Charsets.UTF_8)
val minecraft = libs.versions.minecraft.get()
val versions = listOf(
    "1.21.10",
    minecraft
)

rootProject.description = rootProject.property("project_description").toString()
rootProject.version = if (isSnapshot) "$minecraft-$commitHash" else rootProject.property("plugin_version").toString()
rootProject.group = rootProject.property("project_group").toString()

allprojects {
    apply(plugin = "java-library")
}

tasks {
    withType<Jar> {
        subprojects {
            dependsOn(project.tasks.build)
        }

        // get subproject's built jars
        val jars = subprojects.map { zipTree(it.tasks.jar.get().archiveFile.get().asFile) }

        // merge them into main jar (except their manifests)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(jars) {
            exclude("META-INF/MANIFEST.MF")
        }

        // put behind an action because files don't exist at configuration time
        doFirst {
            // merge all subproject's manifests into main manifest
            jars.forEach { jar ->
                jar.matching { include("META-INF/MANIFEST.MF") }
                    .files.forEach { file ->
                        manifest.from(file)
                    }
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_TOKEN")

    projectId = rootProject.name

    versionName = "${rootProject.name} ${rootProject.version}"
    versionNumber = "${rootProject.version}"
    versionType = if (isSnapshot) "beta" else "release"

    changelog = content

    gameVersions.addAll(versions)

    uploadFile = tasks.jar.get().archiveFile.get()

    loaders.addAll(listOf("paper", "folia", "purpur"))

    syncBodyFrom = rootProject.file("description.md").readText(Charsets.UTF_8)

    autoAddDependsOn = false
    detectLoaders = false
}

hangarPublish {
    publications.register("plugin") {
        apiKey.set(System.getenv("HANGAR_KEY"))

        id.set(rootProject.name)

        version.set("${rootProject.version}")

        channel.set(if (isSnapshot) "Beta" else "Release")

        changelog.set(content)

        platforms {
            paper {
                jar = tasks.jar.flatMap { it.archiveFile }

                platformVersions.set(versions)

                dependencies {
                    hangar("PlaceholderAPI") {
                        required = false
                    }

                    url("ItemsAdder", "https://polymart.org/product/1851/itemsadder") {
                        required = false
                    }

                    url("Oraxen", "https://polymart.org/product/629/oraxen") {
                        required = false
                    }

                    url("Nexo", "https://polymart.org/resource/nexo.6901") {
                        required = false
                    }
                }
            }
        }
    }
}

feather {
    rootDirectory = rootProject.rootDir.toPath()

    val data = git.getGithubCommit("Crazy-Crew/${rootProject.name}")

    val user = data.user

    discord {
        webhook {
            group(rootProject.name.lowercase())
            task("dev-build")

            if (System.getenv("CV_WEBHOOK") != null) {
                post(System.getenv("CV_WEBHOOK"))
            }

            username("Ryder Belserion")

            avatar("https://github.com/ryderbelserion.png")

            embeds {
                embed {
                    color("#ffa347")

                    title("A new dev version of ${rootProject.name} is ready!")

                    fields {
                        field(
                            "Version ${rootProject.version}",
                            listOf(
                                "*Click below to download!*",
                                "<:modrinth:1115307870473420800> [Modrinth](https://modrinth.com/plugin/${rootProject.name.lowercase()}/version/${rootProject.version})",
                                "<:hangar:1139326635313733652> [Hangar](https://hangar.papermc.io/CrazyCrew/${rootProject.name.lowercase()}/versions/${rootProject.version})"
                            ).convertList()
                        )

                        field(
                            ":bug: Report Bugs",
                            "https://github.com/Crazy-Crew/${rootProject.name}/issues"
                        )

                        field(
                            ":hammer: Changelog",
                            content
                        )
                    }
                }
            }
        }

        webhook {
            group(rootProject.name.lowercase())
            task("release-build")

            if (System.getenv("BUILD_WEBHOOK") != null) {
                post(System.getenv("BUILD_WEBHOOK"))
            }

            username(user.getName())

            avatar(user.avatar)

            content("<@&1029922295210311681>")

            embeds {
                embed {
                    color("#1bd96a")

                    title("A new release version of ${rootProject.name} is ready!")

                    fields {
                        field(
                            "Version ${rootProject.version}",
                            listOf(
                                "*Click below to download!*",
                                "<:modrinth:1115307870473420800> [Modrinth](https://modrinth.com/plugin/${rootProject.name.lowercase()}/version/${rootProject.version})",
                                "<:hangar:1139326635313733652> [Hangar](https://hangar.papermc.io/CrazyCrew/${rootProject.name.lowercase()}/versions/${rootProject.version})"
                            ).convertList()
                        )

                        field(
                            ":bug: Report Bugs",
                            "https://github.com/Crazy-Crew/${rootProject.name}/issues"
                        )

                        field(
                            ":hammer: Changelog",
                            "[Click](https://modrinth.com/plugin/${rootProject.name.lowercase()}/version/${rootProject.version})"
                        )
                    }
                }
            }
        }

        webhook {
            group(rootProject.name.lowercase())
            task("failed-build")

            if (System.getenv("CV_WEBHOOK") != null) {
                post(System.getenv("CV_WEBHOOK"))

                username("Ryder Belserion")

                avatar("https://github.com/ryderbelserion.png")
            }

            if (System.getenv("BUILD_WEBHOOK") != null) {
                post(System.getenv("BUILD_WEBHOOK"))

                username(user.getName())

                avatar(user.avatar)
            }

            embeds {
                embed {
                    color("#FF000D")

                    title("Oh no! It failed!")

                    thumbnail("https://raw.githubusercontent.com/ryderbelserion/Branding/refs/heads/main/booze.jpg")

                    fields {
                        field(
                            "The build versioned ${rootProject.version} for project ${rootProject.name} failed.",
                            "The developer is likely already aware, he is just getting drunk.",
                            inline = true
                        )
                    }
                }
            }
        }
    }
}

fun List<String>.convertList(): String {
    val builder = StringBuilder(size)

    forEach {
        builder.append(it).append("\n")
    }

    return builder.toString()
}