import java.awt.Color
import java.io.File
import task.WebhookExtension
import io.papermc.hangarpublishplugin.model.Platforms
import com.lordcodes.turtle.shellRun
import com.ryderbelserion.feather.git.Patcher
import java.io.ByteArrayOutputStream

plugins {
    id("root-plugin")

    id("featherpatcher")
    id("com.modrinth.minotaur")
    id("io.papermc.hangar-publish-plugin")
}

val releaseColor = Color(27, 217, 106)
val betaColor = Color(255, 163, 71)
val logColor = Color(37, 137, 204)

val isBeta = true
val color = if (isBeta) logColor else releaseColor
val repo = if (isBeta) "beta" else "releases"

val type = if (isBeta) "beta" else "release"
val otherType = if (isBeta) "Beta" else "Release"

val msg = "New version of ${rootProject.name} is ready! <@&1029922295210311681>"

val downloads = """
    https://modrinth.com/plugin/${rootProject.name.lowercase()}/version/${rootProject.version}"
    https://hangar.papermc.io/CrazyCrew/${rootProject.name}/versions/${rootProject.version}
""".trimIndent()

val start = shellRun("git", listOf("rev-parse", "--short", "origin/main"))
val end = shellRun("git", listOf("rev-parse", "--short", "HEAD"))

val commitLog = getGitHistory().joinToString(separator = "") { formatGitLog(it) }

val desc = """
  # Release ${rootProject.version}
           
  Spigot support has been completely dropped.
           
  ### Commits
            
  <details>
          
  <summary>Other</summary>
           
  $commitLog
            
  </details>
                
  Report any bugs @ https://github.com/Crazy-Crew/${rootProject.name}/issues
""".trimIndent()

val versions = listOf(
    "1.19",
    "1.19.1",
    "1.19.2",
    "1.19.3",
    "1.19.4"
)

fun getGitHistory(): List<String> {
    val output: String = ByteArrayOutputStream().use { outputStream ->
        project.exec {
            executable("git")
            args("log",  "$start..$end", "--format=format:%h %s")
            standardOutput = outputStream
        }

        outputStream.toString()
    }

    return output.split("\n")
}

fun formatGitLog(commitLog: String): String {
    val hash = commitLog.take(7)
    val message = commitLog.substring(8) // Get message after commit hash + space between
    return "[$hash](https://github.com/Crazy-Crew/${rootProject.name}/commit/$hash) $message<br>"
}

tasks {
    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set(rootProject.name.lowercase())

        versionName.set("${rootProject.name} ${rootProject.version}")
        versionNumber.set(rootProject.version.toString())

        versionType.set(type)

        val file = File("$rootDir/jars")
        if (!file.exists()) file.mkdirs()

        uploadFile.set(layout.buildDirectory.file("$file/${rootProject.name}-${rootProject.version}.jar"))

        autoAddDependsOn.set(true)

        gameVersions.addAll(versions)

        loaders.addAll(listOf("paper", "purpur"))

        changelog.set(desc)
    }
}

hangarPublish {
    publications.register("release") {

        namespace("Ryder Belserion", rootProject.name)
        version.set(rootProject.version as String)
        channel.set(otherType)

        changelog.set(desc)

        apiKey.set(System.getenv("HANGAR_KEY"))

        val file = File("$rootDir/jars")
        if (!file.exists()) file.mkdirs()

        platforms {
            register(Platforms.PAPER) {
                jar.set(layout.buildDirectory.file("$file/${rootProject.name}-${rootProject.version}.jar"))

                platformVersions.set(versions)
            }
        }
    }
}

webhook {
    this.avatar("https://en.gravatar.com/avatar/${WebhookExtension.Gravatar().md5Hex("no-reply@ryderbelserion.com")}.jpeg")

    this.username("Ryder Belserion")

    this.content(msg)

    this.embeds {
        this.embed {
            this.color(color)

            this.fields {
                this.field(
                    "Download: ",
                    downloads
                )

                this.field(
                    "API: ",
                    "https://repo.crazycrew.us/#/$repo/${rootProject.group.toString().replace(".", "/")}/${rootProject.name.lowercase()}-api/${rootProject.version}"
                )
            }

            this.author(
                "${rootProject.name} | Version ${rootProject.version}",
                downloads,
                "https://raw.githubusercontent.com/RyderBelserion/assets/main/crazycrew/png/${rootProject.name}Website.png"
            )
        }

        this.embed {
            this.color(logColor)

            this.title("What changed?")

            this.description("""
                Full Changelogs -> $downloads
            """.trimIndent())
        }
    }

    this.url("DISCORD_WEBHOOK")
}

publishing {
    repositories {
        val repo = if (isBeta) "beta" else "releases"
        maven("https://repo.crazycrew.us/$repo") {
            name = "crazycrew"
            //credentials(PasswordCredentials::class)

            credentials {
                username = System.getenv("REPOSITORY_USERNAME")
                password = System.getenv("REPOSITORY_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "${rootProject.name.lowercase()}-api"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}