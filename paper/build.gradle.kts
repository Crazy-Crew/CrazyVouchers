plugins {
    `paper-plugin`
}

project.group = "${rootProject.group}"

repositories {
    maven("https://repo.momirealms.net/releases/")

    maven("https://repo.hibiscusmc.com/releases/")

    maven("https://repo.nexomc.com/releases/")

    maven("https://repo.oraxen.com/releases/")

    maven("https://maven.devs.beer/")
}

dependencies {
    implementation(project(":common"))
    implementation(libs.fusion.paper)
    implementation(libs.triumph.cmds)
    implementation(libs.metrics)

    compileOnly(libs.bundles.shared)
}

tasks {
    shadowJar {
        listOf(
            "org.spongepowered",
            "org.bstats",
            "org.yaml"
        ).forEach { relocate(it, "libs.$it") }
    }

    runPaper.folia.registerTask()

    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArgs("-Dcom.mojang.eula.agree=true")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(libs.versions.minecraft.get())
    }
}