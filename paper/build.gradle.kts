plugins {
    `config-paper`
}

repositories {
    maven("https://repo.nexomc.com/releases/")

    maven("https://repo.oraxen.com/releases/")

    maven("https://maven.devs.beer/")
}

dependencies {
    implementation(project(path = ":api", configuration = "shadow"))

    implementation(libs.fusion.paper)

    implementation(libs.triumph.cmds)

    implementation(libs.nbt.api)

    implementation(libs.metrics)

    compileOnly(libs.bundles.shared)
}

tasks {
    shadowJar {
        listOf(
            "org.bstats"
        ).forEach {
            relocate(it, "libs.$it")
        }

        archiveBaseName.set("${rootProject.name}-${rootProject.version}")

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }

    compileJava {
        dependsOn(":api:jar")
    }

    runPaper.folia.registerTask()

    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArgs("-Dcom.mojang.eula.agree=true")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(libs.versions.minecraft.get())
    }
}