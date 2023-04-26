plugins {
    id("root-plugin")

    id("com.github.johnrengelman.shadow")

    id("io.papermc.paperweight.userdev")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    shadowJar {
        listOf(
            "de.tr7zw.changeme.nbtapi",
            "org.bstats"
        ).forEach { pack -> relocate(pack, "${rootProject.group}.$pack") }
    }
}