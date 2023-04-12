import task.WebhookExtension
import java.awt.Color

plugins {
    id("crazyvouchers.root-plugin")

    id("featherpatcher") version "0.0.0.2"
}

val releaseUpdate = Color(27, 217, 106)
val betaUpdate = Color(255, 163, 71)
val changeLogs = Color(37, 137, 204)

val beta = settings.versions.beta.get().toBoolean()
val extension = settings.versions.extension.get()

val color = if (beta) betaUpdate else releaseUpdate
val repo = if (beta) "beta" else "releases"

val download = "https://modrinth.com/$extension/${rootProject.name.lowercase()}/version/${rootProject.version}"

val msg = "New version of ${rootProject.name} is ready! <@&1029922295210311681>"

rootProject.version = "2.9.14.3"

val desc = "https://modrinth.com/$extension/${rootProject.name.lowercase()}/version/${rootProject.version}"

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
                    download
                )

                this.field(
                    "API: ",
                    "https://repo.crazycrew.us/#/$repo/${rootProject.group.toString().replace(".", "/")}/${rootProject.name.lowercase()}-api/${rootProject.version}"
                )
            }

            this.author(
                "${rootProject.name} | Version ${rootProject.version}",
                download,
                "https://raw.githubusercontent.com/RyderBelserion/assets/main/crazycrew/png/${rootProject.name}Website.png"
            )
        }

        this.embed {
            this.color(changeLogs)

            this.title("What changed?")

            this.description("""
                » Preventing opening vouchers if inventory is full.
                » Add a toggle to prevent people from opening vouchers in creative.
                » Removed the update checker for Spigot, The option in config.yml is a paperweight.
                
                API:
                 » N/A
                 
                Bugs:
                 » Submit any bugs @ https://github.com/Crazy-Crew/CrazyVouchers/issues
                
                Full Changelog -> $download
            """.trimIndent())
        }
    }

    this.url("DISCORD_WEBHOOK")
}