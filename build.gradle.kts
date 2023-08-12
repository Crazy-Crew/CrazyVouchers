import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    `maven-publish`
    `java-library`

    alias(libs.plugins.shadow)
    alias(libs.plugins.userdev)
    alias(libs.plugins.modrinth)
    alias(libs.plugins.hangar)
}

rootProject.group = "com.badbones69.crazyvouchers"
rootProject.description = "Want to make a paper that can give you an axolotl with a pretty firework display, Look no further!"
rootProject.version = "3.0"

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
val isSnapshot = rootProject.version.toString().contains("snapshot")

val directory = File("$rootDir/jars")

tasks {
    reobfJar {
        if (!directory.exists()) directory.mkdirs()

        outputJar.set(layout.buildDirectory.file("$directory/${rootProject.name}-${rootProject.version}.jar"))
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

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString()
                artifactId = "${rootProject.name.lowercase()}-api"
                version = rootProject.version.toString()

                from(component)
            }
        }

        repositories {
            maven {
                credentials {
                    this.username = System.getenv("gradle_username")
                    this.password = System.getenv("gradle_password")
                }

                if (isSnapshot) {
                    url = uri("https://repo.crazycrew.us/snapshots/")
                    return@maven
                }

                url = uri("https://repo.crazycrew.us/releases/")
            }
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
## New Features:
* Added the ability for an item to have damage applied to it. 
  * You do need a mod to be able to see how much durability an item has by default in order to apply the correct damage.
* Added the ability for armor trim support as you can see below.
```yml
  Trims:
    Item: 'DIAMOND_HELMET'
    Name: '&8&l>&7&l>&cTrim Example&7&l<&8&l<'
    # Only works on items with durability. This will make the item appear more damaged.
    # It does not set the durability but subtracts this number from the durability
    # Durability is 100, It subtracts 50.
    # It cannot be 0.
    DisplayDamage: 50
    DisplayTrim:
      # Available Materials: https://jd.papermc.io/paper/1.20/org/bukkit/inventory/meta/trim/TrimMaterial.html
      Material: 'QUARTZ'
      # Available Patterns: https://jd.papermc.io/paper/1.20/org/bukkit/inventory/meta/trim/TrimPattern.html
      Pattern: 'SENTRY'
    Lore:
      - '&7Use this voucher to get fancy trims.'
      - '&7&l(&6&l!&7&l) &7Right click to redeem.'
    Player: ''
    Glowing: true
    # Available Materials: https://jd.papermc.io/paper/1.20/org/bukkit/inventory/meta/trim/TrimMaterial.html
    # Available Patterns: https://jd.papermc.io/paper/1.20/org/bukkit/inventory/meta/trim/TrimPattern.html
    Items:
      - 'Item:DIAMOND_HELMET, Damage: 50, Trim-Pattern:SENTRY, Trim-Material:QUARTZ, Amount:1, Protection:4, Respiration:1, Aqua_Affinity:3, Unbreaking:3, Thorns:3'
      - 'Item:DIAMOND_CHESTPLATE, Damage: 50, Trim-Pattern:DUNE, Trim-Material:REDSTONE, Amount:1, Protection:4, Unbreaking:3, Thorns:3'
```
    
## Other:
* [Feature Requests](https://github.com/Crazy-Crew/${rootProject.name}/discussions/categories/features)
* [Bug Reports](https://github.com/Crazy-Crew/${rootProject.name}/issues)
""".trimIndent()

val versions = listOf(
    "1.20",
    "1.20.1"
    //"1.20.2"
)

val output = file("$directory/${rootProject.name}-${rootProject.version}.jar")

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