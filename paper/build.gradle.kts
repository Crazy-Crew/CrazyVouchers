plugins {
    id("io.github.goooler.shadow")

    `paper-plugin`
}

dependencies {
    compileOnly(fileTree("$rootDir/libs/compile").include("*.jar"))

    //implementation(libs.metrics)

    implementation(libs.vital)

    compileOnly(libs.placeholder.api)

    compileOnly(libs.itemsadder.api)

    compileOnly(libs.triumph.cmds)

    compileOnly(libs.oraxen.api)

    compileOnly(libs.config.me)

    compileOnly(libs.nbt.api)
}

val component: SoftwareComponent = components["java"]

tasks {
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
                artifactId = "${rootProject.name.lowercase()}-${project.name.lowercase()}-api"
                version = rootProject.version.toString()

                from(component)
            }
        }
    }

    assemble {
        doLast {
            copy {
                from(shadowJar.get())
                into(rootProject.projectDir.resolve("jars"))
            }
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        listOf(
            "com.ryderbelserion.vital",
            //"org.bstats"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        inputs.properties("name" to rootProject.name)
        inputs.properties("version" to project.version)
        inputs.properties("group" to project.group)
        //inputs.properties("authors" to project.properties["authors"])
        inputs.properties("description" to project.properties["description"])
        inputs.properties("website" to project.properties["website"])

        filesMatching("paper-plugin.yml") {
            expand(inputs.properties)
        }
    }
}