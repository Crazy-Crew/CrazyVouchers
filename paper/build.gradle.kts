plugins {
    alias(libs.plugins.paperweight)
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.runPaper)

    `paper-plugin`
}

feather {
    repository("https://repo.oraxen.com/releases")
}

base {
    archivesName.set(rootProject.name)
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)

    implementation(libs.nbtapi)

    compileOnly(libs.placeholderapi)

    compileOnly(libs.itemsadder)

    compileOnly(libs.oraxen)

    api(projects.crazyvouchersCore)
}

val component: SoftwareComponent = components["java"]

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
}

tasks {
    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(libs.versions.minecraft.get())
    }

    publishing {
        repositories {
            maven {
                url = uri("https://repo.crazycrew.us/releases")

                credentials {
                    this.username = System.getenv("gradle_username")
                    this.password = System.getenv("gradle_password")
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString()
                artifactId = "${rootProject.name.lowercase()}-paper-api"
                version = rootProject.version.toString()

                from(component)
            }
        }
    }

    assemble {
        dependsOn(reobfJar)

        doLast {
            copy {
                from(reobfJar.get())
                into(rootProject.projectDir.resolve("jars"))
            }
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        listOf(
            "de.tr7zw.changeme.nbtapi",
            "com.ryderbelserion",
            "ch.jalu"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        inputs.properties("name" to rootProject.name)
        inputs.properties("version" to project.version)
        inputs.properties("group" to project.group)
        inputs.properties("description" to project.properties["description"])
        inputs.properties("apiVersion" to libs.versions.minecraft.get())
        inputs.properties("authors" to project.properties["authors"])
        inputs.properties("website" to project.properties["website"])

        filesMatching("plugin.yml") {
            expand(inputs.properties)
        }
    }
}