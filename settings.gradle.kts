pluginManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")

        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "CrazyVouchers"

listOf(
    "paper",
    "common"
).forEach {
    include(it)
}