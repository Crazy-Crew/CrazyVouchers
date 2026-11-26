import utils.convertList
import utils.updateMarkdown

plugins {
    id("modrinth-plugin")
    id("hangar-plugin")

    `shadow-plugin`
}

dependencies {
    implementation(project(":paper"))
}

tasks {
    shadowJar {
        listOf(
            "com.ryderbelserion.fusion",
            "com.zaxxer.hikari",

            "io.leangen.geantyref",

            "dev.triumphteam.cmd",

            "org.spongepowered",
            "org.jspecify",
            "org.tinylog",
            "org.bstats",
            "org.yaml"
        ).forEach { relocate(it, "libs.$it") }
    }

    jar {
        enabled = false

        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
    }
}

val git = feather.builder

val releaseType = rootProject.ext.get("release_type").toString()
val color = rootProject.property("${releaseType.lowercase()}_color").toString()
val isRelease = releaseType.equals("release", true)
val isWebhookEnabled: Boolean = System.getenv().getOrDefault("is_webhook_enabled", "true").lowercase() == "true"

val lines = rootProject.ext.get("mc_changelog").toString()
val result: String = if (lines.length > 1024) {
    listOf(
        "Click the download links above!"
    ).convertList()
} else {
    lines
}

feather {
    targetDirectory = rootProject.rootDir.toPath()

    val origin = git.getNewestCommit(rootProject.property("repository_owner").toString(),rootProject.name, git.utils.getRemoteCommitHash())

    discord {
        webhook {
            group(rootProject.name.lowercase())
            task("release-build")

            isEnabled(isWebhookEnabled)

            if (System.getenv("BUILD_WEBHOOK") != null) {
                post(System.getenv("BUILD_WEBHOOK"))
            }

            if (isRelease) {
                val user = origin?.user

                username(user?.name ?: "N/A")
                avatar(user?.getAvatar() ?: "N/A")
            } else {
                username(rootProject.property("author_name").toString())

                avatar(rootProject.property("author_avatar").toString())
            }

            embeds {
                embed {
                    color(color)

                    title("The next version of ${rootProject.name} is ready!")

                    if (isRelease) {
                        content("<@&${rootProject.property("discord_role_id").toString()}>")
                    }

                    fields {
                        field(
                            "Version ${rootProject.version}",
                            listOf(
                                "*Click below to download!*",
                                "<:modrinth:1115307870473420800> [Modrinth](https://modrinth.com/plugin/${rootProject.name.lowercase()}/version/${rootProject.version})",
                                "<:hangar:1139326635313733652> [Hangar](https://hangar.papermc.io/${rootProject.property("repository_owner").toString().replace("-", "")}/${rootProject.name.lowercase()}/versions/${rootProject.version})",
                                "",
                                "<:business:1527739792358637598> [Jenkins](https://ci.crazycrew.us/job/${rootProject.name}/lastSuccessfulBuild/)"
                            ).convertList()
                        )

                        field(
                            ":bug: Report Bugs",
                            "https://github.com/${rootProject.property("repository_owner")}/${rootProject.name}/issues"
                        )

                        field(
                            ":hammer: Changelog",
                            result.updateMarkdown()
                        )
                    }
                }
            }
        }

        webhook {
            group(rootProject.name.lowercase())
            task("failed-build")

            isEnabled(isWebhookEnabled)

            if (System.getenv("BUILD_WEBHOOK") != null) {
                post(System.getenv("BUILD_WEBHOOK"))
            }

            username(rootProject.property("mascot_name").toString())

            avatar(rootProject.property("mascot_avatar").toString())

            embeds {
                embed {
                    color(rootProject.property("failed_color").toString())

                    title("Oh no! It failed!")

                    thumbnail("https://raw.githubusercontent.com/ryderbelserion/Branding/refs/heads/main/booze.jpg")

                    fields {
                        field(
                            "The build versioned ${rootProject.version} for project ${rootProject.name} failed.",
                            "The developer is likely already aware, he is just getting drunk."
                        )
                    }
                }
            }
        }
    }
}